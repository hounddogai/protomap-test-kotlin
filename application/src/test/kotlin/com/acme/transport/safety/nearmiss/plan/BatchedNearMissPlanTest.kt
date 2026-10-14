package com.acme.transport.safety.nearmiss.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedNearMissPlanTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedNearMissPlan("code-885", "name-179")
        assertEquals(BatchedNearMissPlanStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to reopened`() {
        val item = newBatchedNearMissPlan("code-885", "name-179")
        try {
            item.moveTo(BatchedNearMissPlanStatus.REOPENED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedNearMissPlanStatus.REOPENED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedNearMissPlan("code-885", "name-179")
        item.status = BatchedNearMissPlanStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedNearMissPlanStatus.OPEN) }
    }
}
