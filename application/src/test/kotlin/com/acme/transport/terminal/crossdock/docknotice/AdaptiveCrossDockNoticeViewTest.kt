package com.acme.transport.terminal.crossdock.docknotice

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveCrossDockNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveCrossDockNotice("code-102", "name-783")
        val view = toAdaptiveCrossDockNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveCrossDockNoticeViews(mutableListOf<AdaptiveCrossDockNotice>(item, item)).size, "views")
    }
}
