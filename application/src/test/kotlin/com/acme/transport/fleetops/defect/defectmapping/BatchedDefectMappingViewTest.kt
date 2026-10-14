package com.acme.transport.fleetops.defect.defectmapping

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedDefectMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedDefectMapping("code-711", "title-955")
        val view = toBatchedDefectMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedDefectMappingViews(mutableListOf<BatchedDefectMapping>(item, item)).size, "views")
    }
}
