package com.acme.transport.fleetops.defect.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualDefectTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualDefectTarget("code-967", "label-69")
        val view = toManualDefectTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualDefectTargetViews(mutableListOf<ManualDefectTarget>(item, item)).size, "views")
    }
}
