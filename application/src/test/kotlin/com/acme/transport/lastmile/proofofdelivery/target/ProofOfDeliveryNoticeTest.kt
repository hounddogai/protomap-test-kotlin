package com.acme.transport.lastmile.proofofdelivery.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryNoticeTest {
    @Test
    fun `starts in new`() {
        val item = newProofOfDeliveryNotice("code-857", "name-440")
        assertEquals(ProofOfDeliveryNoticeStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to queued`() {
        val item = newProofOfDeliveryNotice("code-857", "name-440")
        try {
            item.moveTo(ProofOfDeliveryNoticeStatus.QUEUED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryNoticeStatus.QUEUED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newProofOfDeliveryNotice("code-857", "name-440")
        item.status = ProofOfDeliveryNoticeStatus.FAILED
        assertFails("move") { item.moveTo(ProofOfDeliveryNoticeStatus.NEW) }
    }
}
