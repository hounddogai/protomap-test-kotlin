package com.acme.transport.network.transittime.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictTransitTimeNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictTransitTimeNotice("code-890", "title-738")
        val view = toStrictTransitTimeNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictTransitTimeNoticeViews(mutableListOf<StrictTransitTimeNotice>(item, item)).size, "views")
    }
}
