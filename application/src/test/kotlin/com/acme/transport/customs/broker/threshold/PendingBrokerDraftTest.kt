package com.acme.transport.customs.broker.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class PendingBrokerDraftTest {
    @Test
    fun `starts in requested`() {
        val item = newPendingBrokerDraft("code-305", "label-273")
        assertEquals(PendingBrokerDraftStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newPendingBrokerDraft("code-305", "label-273")
        try {
            item.moveTo(PendingBrokerDraftStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(PendingBrokerDraftStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newPendingBrokerDraft("code-305", "label-273")
        item.status = PendingBrokerDraftStatus.EXPIRED
        assertFails("move") { item.moveTo(PendingBrokerDraftStatus.REQUESTED) }
    }
}
