package com.acme.transport.customs.hold.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedHoldVarianceTest {
    @Test
    fun `starts in proposed`() {
        val item = newBatchedHoldVariance("code-211", "name-913")
        assertEquals(BatchedHoldVarianceStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newBatchedHoldVariance("code-211", "name-913")
        try {
            item.moveTo(BatchedHoldVarianceStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedHoldVarianceStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newBatchedHoldVariance("code-211", "name-913")
        item.status = BatchedHoldVarianceStatus.WITHDRAWN
        assertFails("move") { item.moveTo(BatchedHoldVarianceStatus.PROPOSED) }
    }
}
