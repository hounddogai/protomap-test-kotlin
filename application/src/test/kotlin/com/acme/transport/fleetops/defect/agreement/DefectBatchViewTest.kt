package com.acme.transport.fleetops.defect.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class DefectBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefectBatch("code-762", "name-150")
        val view = toDefectBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefectBatchViews(mutableListOf<DefectBatch>(item, item)).size, "views")
    }
}
