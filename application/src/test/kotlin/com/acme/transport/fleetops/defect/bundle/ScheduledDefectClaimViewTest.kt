package com.acme.transport.fleetops.defect.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledDefectClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledDefectClaim("code-10", "title-418")
        val view = toScheduledDefectClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledDefectClaimViews(mutableListOf<ScheduledDefectClaim>(item, item)).size, "views")
    }
}
