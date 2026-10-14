package com.acme.transport.terminal.traileryard.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedTrailerYardPolicyTest {
    @Test
    fun `starts in created`() {
        val item = newBatchedTrailerYardPolicy("code-425", "label-327")
        assertEquals(BatchedTrailerYardPolicyStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to published`() {
        val item = newBatchedTrailerYardPolicy("code-425", "label-327")
        try {
            item.moveTo(BatchedTrailerYardPolicyStatus.PUBLISHED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedTrailerYardPolicyStatus.PUBLISHED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newBatchedTrailerYardPolicy("code-425", "label-327")
        item.status = BatchedTrailerYardPolicyStatus.RETIRED
        assertFails("move") { item.moveTo(BatchedTrailerYardPolicyStatus.CREATED) }
    }
}
