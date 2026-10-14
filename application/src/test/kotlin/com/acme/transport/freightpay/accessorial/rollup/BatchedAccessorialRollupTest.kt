package com.acme.transport.freightpay.accessorial.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedAccessorialRollupTest {
    @Test
    fun `starts in open`() {
        val item = newBatchedAccessorialRollup("code-246", "title-137")
        assertEquals(BatchedAccessorialRollupStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newBatchedAccessorialRollup("code-246", "title-137")
        try {
            item.moveTo(BatchedAccessorialRollupStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedAccessorialRollupStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newBatchedAccessorialRollup("code-246", "title-137")
        item.status = BatchedAccessorialRollupStatus.ARCHIVED
        assertFails("move") { item.moveTo(BatchedAccessorialRollupStatus.OPEN) }
    }
}
