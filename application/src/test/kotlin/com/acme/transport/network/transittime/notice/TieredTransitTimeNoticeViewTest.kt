package com.acme.transport.network.transittime.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredTransitTimeNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredTransitTimeNotice("code-560", "title-790")
        val view = toTieredTransitTimeNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredTransitTimeNoticeViews(mutableListOf<TieredTransitTimeNotice>(item, item)).size, "views")
    }
}
