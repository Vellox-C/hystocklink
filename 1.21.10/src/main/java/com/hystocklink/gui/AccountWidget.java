package com.hystocklink.gui;

import com.hystocklink.HystockConfig;
import com.hystocklink.HystockLinkClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.client.gui.components.AbstractWidget;

public class AccountWidget extends AbstractWidget {
    private static final int PAD = 6;
    private static final int CONTENT_W = 150;
    private static final int LABEL_H = 9;
    private static final int GAP = 2;
    private static final int BTN_H = 20;
    private static final int HINT_H = 8;
    private static final int CONTENT_H = LABEL_H + GAP + BTN_H + GAP + HINT_H;

    private boolean dragging = false;
    private int dragOffX, dragOffY;

    public AccountWidget(int x, int y) {
        super(x, y, CONTENT_W + 2 * PAD, CONTENT_H + 2 * PAD, Component.empty());
    }

    private boolean isMoveKeyHeld() {
        return HystockLinkClient.isMoveKeyHeld();
    }

    private int cx() { return getX() + PAD; }
    private int cy() { return getY() + PAD; }
    private int btnY() { return cy() + LABEL_H + GAP; }
    private int hintY() { return btnY() + BTN_H + GAP; }

    @Override
    protected void renderWidget(GuiGraphics ctx, int mouseX, int mouseY, float delta) {
        var font = Minecraft.getInstance().font;
        boolean moveMode = isMoveKeyHeld();

        int col   = moveMode ? 0xFF666666 : 0xFFAAAAAA;
        int bgCol = moveMode ? 0x33000000 : 0x88000000;

        // Background fill + outer border
        ctx.fill(getX(), getY(), getX() + getWidth(), getY() + getHeight(), bgCol);
        ctx.fill(getX(), getY(), getX() + getWidth(), getY() + 1, col);
        ctx.fill(getX(), getY() + getHeight() - 1, getX() + getWidth(), getY() + getHeight(), col);
        ctx.fill(getX(), getY() + 1, getX() + 1, getY() + getHeight() - 1, col);
        ctx.fill(getX() + getWidth() - 1, getY() + 1, getX() + getWidth(), getY() + getHeight() - 1, col);

        // Username label
        String label = "Active: " + Minecraft.getInstance().getUser().getName();
        if (font.width(label) > CONTENT_W) {
            String ellipsis = "...";
            label = font.plainSubstrByWidth(label, CONTENT_W - font.width(ellipsis)) + ellipsis;
        }
        ctx.drawString(font, label, cx(), cy(), col, false);

        // Button
        boolean hovered = !moveMode
                && mouseX >= cx() && mouseX < cx() + CONTENT_W
                && mouseY >= btnY() && mouseY < btnY() + BTN_H;
        int btnBg  = hovered ? 0xAA000000 : bgCol;
        int btnBdr = hovered ? 0xFFCCCCCC : col;
        int btnTxt = hovered ? 0xFFFFFFFF : col;

        ctx.fill(cx(), btnY(), cx() + CONTENT_W, btnY() + BTN_H, btnBg);
        ctx.fill(cx(),                  btnY(),             cx() + CONTENT_W, btnY() + 1,          btnBdr);
        ctx.fill(cx(),                  btnY() + BTN_H - 1, cx() + CONTENT_W, btnY() + BTN_H,      btnBdr);
        ctx.fill(cx(),                  btnY() + 1,         cx() + 1,          btnY() + BTN_H - 1,  btnBdr);
        ctx.fill(cx() + CONTENT_W - 1,  btnY() + 1,         cx() + CONTENT_W, btnY() + BTN_H - 1,  btnBdr);
        ctx.drawCenteredString(font, "Activate Account", cx() + CONTENT_W / 2, btnY() + 6, btnTxt);

        // Hint text
        String keyName = HystockLinkClient.KEY_MOVE != null
                ? HystockLinkClient.KEY_MOVE.getTranslatedKeyMessage().getString()
                : "LCTRL";
        ctx.drawString(font, "Hold " + keyName + " to move", cx(), hintY(), col, false);
    }

    public void beginDrag(int mx, int my) {
        dragOffX = mx - getX();
        dragOffY = my - getY();
        dragging = true;
    }

    public void performDrag(int mx, int my) {
        int newX = mx - dragOffX;
        int newY = my - dragOffY;
        Screen current = Minecraft.getInstance().screen;
        if (current != null) {
            newX = Math.max(0, Math.min(current.width - getWidth(), newX));
            newY = Math.max(0, Math.min(current.height - getHeight(), newY));
        }
        setX(newX);
        setY(newY);
    }

    public void endDrag() {
        dragging = false;
        HystockConfig.save(getX(), getY());
    }

    public boolean isDragging() {
        return dragging;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean b) {
        if (event.button() != 0 || !isMouseOver(event.x(), event.y())) return false;
        if (isMoveKeyHeld()) {
            beginDrag((int) event.x(), (int) event.y());
        } else if (event.y() >= btnY() && event.y() < btnY() + BTN_H) {
            playDownSound(Minecraft.getInstance().getSoundManager());
            Minecraft.getInstance().setScreen(new ActivationScreen());
        }
        // Returning true for ALL clicks within bounds — this stops MC's dispatch from
        // continuing to lower-priority children (Essential elements underneath).
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
