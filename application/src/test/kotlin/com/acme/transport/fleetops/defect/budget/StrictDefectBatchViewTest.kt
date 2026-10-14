package com.acme.transport.fleetops.defect.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictDefectBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictDefectBatch("code-781", "name-505")
        val view = toStrictDefectBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictDefectBatchViews(mutableListOf<StrictDefectBatch>(item, item)).size, "views")
    }
}
