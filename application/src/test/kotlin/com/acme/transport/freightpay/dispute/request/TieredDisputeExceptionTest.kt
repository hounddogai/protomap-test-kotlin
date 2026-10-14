package com.acme.transport.freightpay.dispute.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredDisputeExceptionTest {
    @Test
    fun `starts in open`() {
        val item = newTieredDisputeException("code-568", "name-613")
        assertEquals(TieredDisputeExceptionStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in review`() {
        val item = newTieredDisputeException("code-568", "name-613")
        try {
            item.moveTo(TieredDisputeExceptionStatus.IN_REVIEW)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredDisputeExceptionStatus.IN_REVIEW, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newTieredDisputeException("code-568", "name-613")
        item.status = TieredDisputeExceptionStatus.ARCHIVED
        assertFails("move") { item.moveTo(TieredDisputeExceptionStatus.OPEN) }
    }
}
