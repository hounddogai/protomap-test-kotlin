package com.acme.transport.fleetops.defect.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class StrictDefectBatchTest {
    @Test
    fun `starts in idle`() {
        val item = newStrictDefectBatch("code-781", "name-505")
        assertEquals(StrictDefectBatchStatus.IDLE, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to warming`() {
        val item = newStrictDefectBatch("code-781", "name-505")
        try {
            item.moveTo(StrictDefectBatchStatus.WARMING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(StrictDefectBatchStatus.WARMING, item.status, "status")
    }

    @Test
    fun `rejects moves from stopped`() {
        val item = newStrictDefectBatch("code-781", "name-505")
        item.status = StrictDefectBatchStatus.STOPPED
        assertFails("move") { item.moveTo(StrictDefectBatchStatus.IDLE) }
    }
}
