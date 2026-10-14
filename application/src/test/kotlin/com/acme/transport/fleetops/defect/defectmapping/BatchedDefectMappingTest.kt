package com.acme.transport.fleetops.defect.defectmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedDefectMappingTest {
    @Test
    fun `starts in draft`() {
        val item = newBatchedDefectMapping("code-711", "title-955")
        assertEquals(BatchedDefectMappingStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to active`() {
        val item = newBatchedDefectMapping("code-711", "title-955")
        try {
            item.moveTo(BatchedDefectMappingStatus.ACTIVE)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedDefectMappingStatus.ACTIVE, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newBatchedDefectMapping("code-711", "title-955")
        item.status = BatchedDefectMappingStatus.CLOSED
        assertFails("move") { item.moveTo(BatchedDefectMappingStatus.DRAFT) }
    }
}
