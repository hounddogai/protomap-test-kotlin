package com.acme.transport.fleetops.defect.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedDefectProfileTest {
    @Test
    fun `starts in requested`() {
        val item = newBatchedDefectProfile("code-757", "name-557")
        assertEquals(BatchedDefectProfileStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newBatchedDefectProfile("code-757", "name-557")
        try {
            item.moveTo(BatchedDefectProfileStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedDefectProfileStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newBatchedDefectProfile("code-757", "name-557")
        item.status = BatchedDefectProfileStatus.EXPIRED
        assertFails("move") { item.moveTo(BatchedDefectProfileStatus.REQUESTED) }
    }
}
