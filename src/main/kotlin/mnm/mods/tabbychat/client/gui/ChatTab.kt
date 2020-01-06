package mnm.mods.tabbychat.client.gui

import com.mojang.blaze3d.systems.RenderSystem
import mnm.mods.tabbychat.api.Channel
import mnm.mods.tabbychat.api.ChannelStatus
import mnm.mods.tabbychat.client.TabbyChatClient
import mnm.mods.tabbychat.client.gui.component.GuiComponent
import mnm.mods.tabbychat.client.gui.settings.GuiSettingsScreen
import mnm.mods.tabbychat.util.*
import net.minecraft.client.gui.screen.Screen

class ChatTab(private val channel: Channel) : GuiComponent() {

    val text: String
        get() {
            return when (ChatBox.status[channel]) {
                ChannelStatus.ACTIVE -> "[${channel.displayName}]"
                ChannelStatus.UNREAD -> "<${channel.displayName}>"
                else -> channel.displayName
            }
        }

    init {
        val chan = "<${channel.displayName}>"
        minimumSize = Dim(mc.fontRenderer.getStringWidth(chan) + 8, 14)
    }

    override fun mouseClicked(x: Double, y: Double, button: Int): Boolean {
        if (super.mouseClicked(x, y, button)) {
            if (button == 0) {
                if (Screen.hasShiftDown()) {
                    // Remove channel
                    ChatBox.removeChannel(this.channel)
                } else {
                    // Enable channel, disable others
                    ChatBox.activeChannel = this.channel
                }
            } else if (button == 1) {
                // Open channel options
                openSettings()
            } else if (button == 2) {
                // middle click
                ChatBox.removeChannel(this.channel)
            } else {
                return false
            }
            return true
        }
        return false
    }

    override fun isValidButton(button: Int): Boolean {
        return true
    }

    private fun openSettings() {
        mc.displayGuiScreen(GuiSettingsScreen(channel))
    }

    override fun render(x: Int, y: Int, parTicks: Float) {
        val status = ChatBox.status[channel]
        if (mc.ingameGUI.chatGUI.chatOpen
                || status != null && status > ChannelStatus.PINGED && TabbyChatClient.settings.general.unreadFlashing
                || TabbyChatClient.settings.advanced.visibility === LocalVisibility.ALWAYS) {
            val loc = location
            RenderSystem.enableBlend()
            RenderSystem.color4f(1f, 1f, 1f, mc.gameSettings.chatOpacity.toFloat())
            drawModalCorners(getStatusModal(loc.contains(x, y)))

            val txtX = loc.xCenter
            val txtY = loc.yCenter - 2

            val (red, green, blue) = primaryColorProperty
            val color = Color.getHex(red, green, blue, (mc.gameSettings.chatOpacity * 255).toInt())
            this.drawCenteredString(mc.fontRenderer, this.text, txtX, txtY, color)
            RenderSystem.disableBlend()
        }
    }

    private fun getStatusModal(hovered: Boolean): TexturedModal {
        if (hovered) {
            return HOVERED
        }
        return when (ChatBox.status[channel]) {
            ChannelStatus.ACTIVE -> ACTIVE
            ChannelStatus.UNREAD -> UNREAD
            ChannelStatus.PINGED -> PINGED
            else -> NONE
        }
    }

    companion object {

        private val ACTIVE = TexturedModal(ChatBox.GUI_LOCATION, 0, 0, 50, 14)
        private val UNREAD = TexturedModal(ChatBox.GUI_LOCATION, 50, 0, 50, 14)
        private val PINGED = TexturedModal(ChatBox.GUI_LOCATION, 100, 0, 50, 14)
        private val HOVERED = TexturedModal(ChatBox.GUI_LOCATION, 150, 0, 50, 14)
        private val NONE = TexturedModal(ChatBox.GUI_LOCATION, 200, 0, 50, 14)
    }
}
