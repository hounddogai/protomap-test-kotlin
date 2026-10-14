package com.acme.transport.safety.nearmiss.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedNearMissVarianceTest {
    @Test
    fun `starts in draft`() {
        val item = newBatchedNearMissVariance("code-443", "title-808")
        assertEquals(BatchedNearMissVarianceStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newBatchedNearMissVariance("code-443", "title-808")
        try {
            item.moveTo(BatchedNearMissVarianceStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedNearMissVarianceStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newBatchedNearMissVariance("code-443", "title-808")
        item.status = BatchedNearMissVarianceStatus.CLOSED
        assertFails("move") { item.moveTo(BatchedNearMissVarianceStatus.DRAFT) }
    }
}
