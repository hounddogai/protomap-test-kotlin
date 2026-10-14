package com.acme.transport.lastmile.proofofdelivery.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryExceptionTest {
    @Test
    fun `starts in new`() {
        val item = newProofOfDeliveryException("code-224", "name-844")
        assertEquals(ProofOfDeliveryExceptionStatus.NEW, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to failed`() {
        val item = newProofOfDeliveryException("code-224", "name-844")
        try {
            item.moveTo(ProofOfDeliveryExceptionStatus.FAILED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryExceptionStatus.FAILED, item.status, "status")
    }

    @Test
    fun `rejects moves from failed`() {
        val item = newProofOfDeliveryException("code-224", "name-844")
        item.status = ProofOfDeliveryExceptionStatus.FAILED
        assertFails("move") { item.moveTo(ProofOfDeliveryExceptionStatus.NEW) }
    }
}
