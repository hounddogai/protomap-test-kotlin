package com.acme.transport.fleetops.defect.defectrecord

import kotlin.test.Test
import kotlin.test.assertEquals

class DefectQuotaViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefectQuota("code-926", "title-848")
        val view = toDefectQuotaView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefectQuotaViews(mutableListOf<DefectQuota>(item, item)).size, "views")
    }
}
