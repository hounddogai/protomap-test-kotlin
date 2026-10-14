package com.acme.transport.customs.clearance.primaryclearanceoverride

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyClearanceNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyClearanceNotice("code-998", "label-225")
        val view = toLegacyClearanceNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyClearanceNoticeViews(mutableListOf<LegacyClearanceNotice>(item, item)).size, "views")
    }
}
