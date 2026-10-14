package com.acme.transport.linehaul.relay.group

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedRelayNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedRelayNotice("code-835", "title-912")
        val view = toCappedRelayNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedRelayNoticeViews(mutableListOf<CappedRelayNotice>(item, item)).size, "views")
    }
}
