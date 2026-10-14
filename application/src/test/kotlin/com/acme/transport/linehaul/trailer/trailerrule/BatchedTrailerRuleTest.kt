package com.acme.transport.linehaul.trailer.trailerrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class BatchedTrailerRuleTest {
    @Test
    fun `starts in requested`() {
        val item = newBatchedTrailerRule("code-118", "label-203")
        assertEquals(BatchedTrailerRuleStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newBatchedTrailerRule("code-118", "label-203")
        try {
            item.moveTo(BatchedTrailerRuleStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(BatchedTrailerRuleStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newBatchedTrailerRule("code-118", "label-203")
        item.status = BatchedTrailerRuleStatus.EXPIRED
        assertFails("move") { item.moveTo(BatchedTrailerRuleStatus.REQUESTED) }
    }
}
