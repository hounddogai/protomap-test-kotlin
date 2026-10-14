package com.acme.transport.fleetops.defect.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedDefectProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedDefectProfile("code-757", "name-557")
        val view = toBatchedDefectProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedDefectProfileViews(mutableListOf<BatchedDefectProfile>(item, item)).size, "views")
    }
}
