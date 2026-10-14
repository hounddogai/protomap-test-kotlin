package com.acme.transport.emissions.evcharge.batchedchargeslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingEvChargeBatchTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingEvChargeBatch("code-479", "label-319")
        assertEquals(PendingEvChargeBatchStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newPendingEvChargeBatch("code-479", "label-319")
        try {
            item.moveTo(PendingEvChargeBatchStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingEvChargeBatchStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingEvChargeBatch("code-479", "label-319")
        item.status = PendingEvChargeBatchStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingEvChargeBatchStatus.REQUESTED) }
    }
}
