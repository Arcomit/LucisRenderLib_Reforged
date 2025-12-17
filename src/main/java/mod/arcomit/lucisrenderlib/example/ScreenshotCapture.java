package mod.arcomit.lucisrenderlib.example;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 截图捕获工具类，用于导出当前渲染目标的内容
 * 支持HDR渲染目标、Iris shader FBO和默认渲染目标
 */
public class ScreenshotCapture {
    private static final Logger LOGGER = LoggerFactory.getLogger(ScreenshotCapture.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss");
    public static boolean captureRequested = false;

    /**
     * 请求截图导出
     * 在需要导出的地方调用此方法，将在下一帧执行截图
     */
    public static void requestExport() {
        captureRequested = true;
    }

    /**
     * 从当前绑定的FBO截图（标准8位RGBA）
     */
    public static void exportCurrentRenderTarget() {
        Minecraft mc = Minecraft.getInstance();
        int width = mc.getMainRenderTarget().width;
        int height = mc.getMainRenderTarget().height;

        try {
            RenderSystem.assertOnRenderThread();

            // 获取当前绑定的FBO用于调试
            int currentFBO = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
            LOGGER.info("正在从FBO {} 截图，尺寸: {}x{}", currentFBO, width, height);

            // RGBA = 4 bytes per pixel
            int size = width * height * 4;
            ByteBuffer buffer = MemoryUtil.memAlloc(size);

            try {
                // 从当前绑定的帧缓冲读取像素到buffer
                GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

                // 创建NativeImage并从buffer复制数据
                NativeImage image = new NativeImage(width, height, false);

                // 逐像素复制 (效率较低但安全)
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int index = (y * width + x) * 4;
                        int r = buffer.get(index) & 0xFF;
                        int g = buffer.get(index + 1) & 0xFF;
                        int b = buffer.get(index + 2) & 0xFF;
                        // 强制设置alpha为255（完全不透明）
                        int a = 0xFF;

                        // NativeImage使用ABGR格式存储
                        int color = (a << 24) | (b << 16) | (g << 8) | r;
                        image.setPixelRGBA(x, y, color);
                    }
                }

                // OpenGL的坐标原点在左下角，需要翻转Y轴
                image.flipY();

                Path gameDir = mc.gameDirectory.toPath();
                String timestamp = LocalDateTime.now().format(FORMATTER);
                String filename = "custom_render_" + timestamp;
                Path outputPath = gameDir.resolve("screenshots").resolve(filename + ".png");

                // 确保目录存在
                if (!outputPath.getParent().toFile().exists()) {
                    outputPath.getParent().toFile().mkdirs();
                }

                image.writeToFile(outputPath);
                image.close();

                mc.gui.getChat().addMessage(Component.literal("渲染目标已导出至: " + outputPath));
                LOGGER.info("成功导出渲染目标至: {}", outputPath);

            } finally {
                // 确保释放buffer
                MemoryUtil.memFree(buffer);
            }

        } catch (Exception e) {
            LOGGER.error("导出渲染目标失败", e);
            mc.gui.getChat().addMessage(Component.literal("§c导出渲染目标失败: " + e.getMessage()));
        }
    }

    /**
     * 从纹理ID截图（支持HDR纹理）
     * @param textureId OpenGL纹理ID
     * @param width 纹理宽度
     * @param height 纹理高度
     * @param filenameSuffix 文件名后缀
     */
    public static void exportFromTexture(int textureId, int width, int height, String filenameSuffix) {
        Minecraft mc = Minecraft.getInstance();

        try {
            RenderSystem.assertOnRenderThread();

            LOGGER.info("正在从纹理 {} 截图，尺寸: {}x{}", textureId, width, height);

            // 创建临时FBO用于读取纹理
            int tempFbo = GL30.glGenFramebuffers();
            int previousFbo = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);

            try {
                // 绑定临时FBO
                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, tempFbo);

                // 附加纹理到FBO
                GL30.glFramebufferTexture2D(
                        GL30.GL_FRAMEBUFFER,
                        GL30.GL_COLOR_ATTACHMENT0,
                        GL11.GL_TEXTURE_2D,
                        textureId,
                        0
                );

                // 检查FBO状态
                int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
                if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
                    LOGGER.error("临时FBO不完整，状态: 0x{}", Integer.toHexString(status));
                    mc.gui.getChat().addMessage(Component.literal("§c截图失败：FBO不完整"));
                    return;
                }

                // 读取像素数据
                int size = width * height * 4;
                ByteBuffer buffer = MemoryUtil.memAlloc(size);

                try {
                    // 从FBO读取像素
                    GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

                    // 创建图像
                    NativeImage image = new NativeImage(width, height, false);

                    // 复制像素数据
                    for (int y = 0; y < height; y++) {
                        for (int x = 0; x < width; x++) {
                            int index = (y * width + x) * 4;
                            int r = buffer.get(index) & 0xFF;
                            int g = buffer.get(index + 1) & 0xFF;
                            int b = buffer.get(index + 2) & 0xFF;
                            // 强制设置alpha为255（完全不透明），因为HDR纹理可能没有alpha通道
                            int a = 0xFF;

                            // NativeImage使用ABGR格式存储
                            int color = (a << 24) | (b << 16) | (g << 8) | r;
                            image.setPixelRGBA(x, y, color);
                        }
                    }

                    // 翻转Y轴
                    image.flipY();

                    // 保存文件
                    Path gameDir = mc.gameDirectory.toPath();
                    String timestamp = LocalDateTime.now().format(FORMATTER);
                    String filename = "hdr_render_" + filenameSuffix + "_" + timestamp;
                    Path outputPath = gameDir.resolve("screenshots").resolve(filename + ".png");

                    if (!outputPath.getParent().toFile().exists()) {
                        outputPath.getParent().toFile().mkdirs();
                    }

                    image.writeToFile(outputPath);
                    image.close();

                    mc.gui.getChat().addMessage(Component.literal("HDR渲染已导出至: " + outputPath));
                    LOGGER.info("成功导出HDR渲染至: {}", outputPath);

                } finally {
                    MemoryUtil.memFree(buffer);
                }

            } finally {
                // 恢复之前的FBO
                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFbo);
                // 删除临时FBO
                GL30.glDeleteFramebuffers(tempFbo);
            }

        } catch (Exception e) {
            LOGGER.error("从纹理导出失败", e);
            mc.gui.getChat().addMessage(Component.literal("§c从纹理导出失败: " + e.getMessage()));
        }
    }

    /**
     * 从FBO的多个颜色附件截图
     * @param fboId 帧缓冲对象ID，如果为-1则使用当前绑定的FBO
     * @param attachmentCount 颜色附件数量（1-8）
     * @param width 宽度
     * @param height 高度
     * @param filenamePrefix 文件名前缀
     */
    public static void exportMultipleAttachments(int fboId, int attachmentCount, int width, int height, String filenamePrefix) {
        Minecraft mc = Minecraft.getInstance();

        try {
            RenderSystem.assertOnRenderThread();

            int previousFbo = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);

            // 如果指定了FBO ID，绑定到该FBO
            if (fboId != -1) {
                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboId);
            }

            LOGGER.info("正在从FBO {} 截取 {} 个颜色附件，尺寸: {}x{}",
                    fboId == -1 ? previousFbo : fboId, attachmentCount, width, height);

            // 检查FBO完整性
            int status = GL30.glCheckFramebufferStatus(GL30.GL_FRAMEBUFFER);
            if (status != GL30.GL_FRAMEBUFFER_COMPLETE) {
                LOGGER.error("FBO不完整，状态: 0x{}", Integer.toHexString(status));
                mc.gui.getChat().addMessage(Component.literal("§c截图失败：FBO不完整"));
                return;
            }

            String timestamp = LocalDateTime.now().format(FORMATTER);
            Path screenshotDir = mc.gameDirectory.toPath().resolve("screenshots");

            if (!screenshotDir.toFile().exists()) {
                screenshotDir.toFile().mkdirs();
            }

            // 逐个读取每个颜色附件
            for (int i = 0; i < attachmentCount; i++) {
                int attachment = GL30.GL_COLOR_ATTACHMENT0 + i;

                // 设置读取缓冲
                GL11.glReadBuffer(attachment);

                try {
                    // 分配内存 (RGBA = 4 bytes per pixel)
                    int size = width * height * 4;
                    ByteBuffer buffer = MemoryUtil.memAlloc(size);

                    try {
                        // 读取像素 - 使用RGBA以确保alpha通道正确读取
                        GL11.glReadPixels(0, 0, width, height, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buffer);

                        // 创建图像
                        NativeImage image = new NativeImage(width, height, false);

                        // 复制像素数据
                        for (int y = 0; y < height; y++) {
                            for (int x = 0; x < width; x++) {
                                int index = (y * width + x) * 4;
                                int r = buffer.get(index) & 0xFF;
                                int g = buffer.get(index + 1) & 0xFF;
                                int b = buffer.get(index + 2) & 0xFF;
                                // 强制设置alpha为255（完全不透明），因为FBO附件可能没有alpha通道
                                int a = 0xFF;

                                // NativeImage使用ABGR格式存储
                                int color = (a << 24) | (b << 16) | (g << 8) | r;
                                image.setPixelRGBA(x, y, color);
                            }
                        }

                        // 翻转Y轴
                        image.flipY();

                        // 保存文件
                        String attachmentName = getAttachmentName(i);
                        String filename = filenamePrefix + "_" + attachmentName + "_" + timestamp + ".png";
                        Path outputPath = screenshotDir.resolve(filename);

                        image.writeToFile(outputPath);
                        image.close();

                        LOGGER.info("成功导出附件 {} 至: {}", attachmentName, outputPath);

                    } finally {
                        MemoryUtil.memFree(buffer);
                    }

                } catch (Exception e) {
                    LOGGER.error("导出附件 {} 失败", i, e);
                }
            }

            // 恢复之前的FBO
            if (fboId != -1) {
                GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFbo);
            }

            mc.gui.getChat().addMessage(Component.literal(
                    String.format("已导出 %d 个颜色附件至 screenshots/", attachmentCount)));

        } catch (Exception e) {
            LOGGER.error("导出多个附件失败", e);
            mc.gui.getChat().addMessage(Component.literal("§c导出多个附件失败: " + e.getMessage()));
        }
    }

    /**
     * 从HDRTarget截取所有附件（包括普通颜色和emissive）
     * @param colorTextureId 主颜色纹理ID
     * @param emissiveTextureId 发光纹理ID（如果为-1则跳过）
     * @param width 宽度
     * @param height 高度
     * @param filenamePrefix 文件名前缀
     */
    public static void exportHDRTargetAllAttachments(int colorTextureId, int emissiveTextureId,
                                                      int width, int height, String filenamePrefix) {
        // 导出主颜色
        exportFromTexture(colorTextureId, width, height, filenamePrefix + "_color");

        // 如果有emissive附件，也导出
        if (emissiveTextureId != -1) {
            exportFromTexture(emissiveTextureId, width, height, filenamePrefix + "_emissive");
        }
    }

    /**
     * 获取附件的友好名称
     */
    private static String getAttachmentName(int index) {
        return "attachment" + index;
    }
}

