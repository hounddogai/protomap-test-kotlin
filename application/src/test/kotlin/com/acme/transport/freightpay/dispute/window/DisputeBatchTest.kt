package com.acme.transport.freightpay.dispute.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DisputeBatchTest {
    @Test
    fun `starts in open`() {
        val item = newDisputeBatch("code-719", "title-641")
        assertEquals(DisputeBatchStatus.OPEN, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to resolved`() {
        val item = newDisputeBatch("code-719", "title-641")
        try {
            item.moveTo(DisputeBatchStatus.RESOLVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DisputeBatchStatus.RESOLVED, item.status, "status")
    }

    @Test
    fun `rejects moves from archived`() {
        val item = newDisputeBatch("code-719", "title-641")
        item.status = DisputeBatchStatus.ARCHIVED
        assertFails("move") { item.moveTo(DisputeBatchStatus.OPEN) }
    }
}
