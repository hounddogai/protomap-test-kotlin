package com.acme.transport.fleetops.utilization.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedUtilizationTierTest {
    @Test
    fun `starts in requested`() {
        val item = newBatchedUtilizationTier("code-311", "label-583")
        assertEquals(BatchedUtilizationTierStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to expired`() {
        val item = newBatchedUtilizationTier("code-311", "label-583")
        try {
            item.moveTo(BatchedUtilizationTierStatus.EXPIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedUtilizationTierStatus.EXPIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newBatchedUtilizationTier("code-311", "label-583")
        item.status = BatchedUtilizationTierStatus.EXPIRED
        assertFails("move") { item.moveTo(BatchedUtilizationTierStatus.REQUESTED) }
    }
}
