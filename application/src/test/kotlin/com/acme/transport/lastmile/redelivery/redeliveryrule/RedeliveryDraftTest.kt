package com.acme.transport.lastmile.redelivery.redeliveryrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RedeliveryDraftTest {
    @Test
    fun `starts in requested`() {
        val item = newRedeliveryDraft("code-383", "title-352")
        assertEquals(RedeliveryDraftStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newRedeliveryDraft("code-383", "title-352")
        try {
            item.moveTo(RedeliveryDraftStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RedeliveryDraftStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newRedeliveryDraft("code-383", "title-352")
        item.status = RedeliveryDraftStatus.EXPIRED
        assertFails("move") { item.moveTo(RedeliveryDraftStatus.REQUESTED) }
    }
}
