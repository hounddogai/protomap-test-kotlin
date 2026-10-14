package com.acme.transport.customs.inspection.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class GlobalInspectionBatchTest {
    @Test
    fun `starts in open`() {
        val item = newGlobalInspectionBatch("code-908", "name-843")
        assertEquals(GlobalInspectionBatchStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newGlobalInspectionBatch("code-908", "name-843")
        try {
            item.moveTo(GlobalInspectionBatchStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(GlobalInspectionBatchStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newGlobalInspectionBatch("code-908", "name-843")
        item.status = GlobalInspectionBatchStatus.ARCHIVED
        assertFails("move") { item.moveTo(GlobalInspectionBatchStatus.OPEN) }
    }
}
