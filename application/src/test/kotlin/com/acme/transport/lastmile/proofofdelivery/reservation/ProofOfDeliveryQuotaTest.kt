package com.acme.transport.lastmile.proofofdelivery.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryQuotaTest {
    @Test
    fun `starts in requested`() {
        val item = newProofOfDeliveryQuota("code-296", "name-481")
        assertEquals(ProofOfDeliveryQuotaStatus.REQUESTED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to granted`() {
        val item = newProofOfDeliveryQuota("code-296", "name-481")
        try {
            item.moveTo(ProofOfDeliveryQuotaStatus.GRANTED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryQuotaStatus.GRANTED, item.status, "status")
    }

    @Test
    fun `rejects moves from expired`() {
        val item = newProofOfDeliveryQuota("code-296", "name-481")
        item.status = ProofOfDeliveryQuotaStatus.EXPIRED
        assertFails("move") { item.moveTo(ProofOfDeliveryQuotaStatus.REQUESTED) }
    }
}
