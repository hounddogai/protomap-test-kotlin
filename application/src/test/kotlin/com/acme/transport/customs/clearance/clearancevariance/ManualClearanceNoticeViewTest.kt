package com.acme.transport.customs.clearance.clearancevariance

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualClearanceNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualClearanceNotice("code-239", "label-891")
        val view = toManualClearanceNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualClearanceNoticeViews(mutableListOf<ManualClearanceNotice>(item, item)).size, "views")
    }
}
