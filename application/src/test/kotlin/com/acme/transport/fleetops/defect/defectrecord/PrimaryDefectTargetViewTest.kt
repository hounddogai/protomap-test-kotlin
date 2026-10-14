package com.acme.transport.fleetops.defect.defectrecord

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryDefectTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryDefectTarget("code-967", "label-85")
        val view = toPrimaryDefectTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryDefectTargetViews(mutableListOf<PrimaryDefectTarget>(item, item)).size, "views")
    }
}
