package com.acme.transport.lastmile.proofofdelivery.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class RollingProofOfDeliveryDraftTest {
    @Test
    fun `starts in draft`() {
        val item = newRollingProofOfDeliveryDraft("code-831", "name-503")
        assertEquals(RollingProofOfDeliveryDraftStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to pending`() {
        val item = newRollingProofOfDeliveryDraft("code-831", "name-503")
        try {
            item.moveTo(RollingProofOfDeliveryDraftStatus.PENDING)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(RollingProofOfDeliveryDraftStatus.PENDING, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newRollingProofOfDeliveryDraft("code-831", "name-503")
        item.status = RollingProofOfDeliveryDraftStatus.CLOSED
        assertFails("move") { item.moveTo(RollingProofOfDeliveryDraftStatus.DRAFT) }
    }
}
