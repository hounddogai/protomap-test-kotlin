package com.acme.transport.fleetops.defect.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class DefectProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefectProfile("code-185", "label-194")
        val view = toDefectProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefectProfileViews(mutableListOf<DefectProfile>(item, item)).size, "views")
    }
}
