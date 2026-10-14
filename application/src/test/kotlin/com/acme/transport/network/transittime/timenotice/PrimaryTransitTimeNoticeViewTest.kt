package com.acme.transport.network.transittime.timenotice

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryTransitTimeNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryTransitTimeNotice("code-629", "label-21")
        val view = toPrimaryTransitTimeNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryTransitTimeNoticeViews(mutableListOf<PrimaryTransitTimeNotice>(item, item)).size, "views")
    }
}
