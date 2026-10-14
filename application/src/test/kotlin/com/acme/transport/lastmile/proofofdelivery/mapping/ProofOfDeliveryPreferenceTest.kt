package com.acme.transport.lastmile.proofofdelivery.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryPreferenceTest {
    @Test
    fun `starts in planned`() {
        val item = newProofOfDeliveryPreference("code-343", "label-145")
        assertEquals(ProofOfDeliveryPreferenceStatus.PLANNED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to in progress`() {
        val item = newProofOfDeliveryPreference("code-343", "label-145")
        try {
            item.moveTo(ProofOfDeliveryPreferenceStatus.IN_PROGRESS)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryPreferenceStatus.IN_PROGRESS, item.status, "status")
    }

    @Test
    fun `rejects moves from cancelled`() {
        val item = newProofOfDeliveryPreference("code-343", "label-145")
        item.status = ProofOfDeliveryPreferenceStatus.CANCELLED
        assertFails("move") { item.moveTo(ProofOfDeliveryPreferenceStatus.PLANNED) }
    }
}
