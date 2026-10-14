package com.acme.transport.lastmile.proofofdelivery.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryDraftTest {
    @Test
    fun `starts in requested`() {
        val item = newProofOfDeliveryDraft("code-848", "title-561")
        assertEquals(ProofOfDeliveryDraftStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to revoked`() {
        val item = newProofOfDeliveryDraft("code-848", "title-561")
        try {
            item.moveTo(ProofOfDeliveryDraftStatus.REVOKED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryDraftStatus.REVOKED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newProofOfDeliveryDraft("code-848", "title-561")
        item.status = ProofOfDeliveryDraftStatus.EXPIRED
        assertFails("move") { item.moveTo(ProofOfDeliveryDraftStatus.REQUESTED) }
    }
}
