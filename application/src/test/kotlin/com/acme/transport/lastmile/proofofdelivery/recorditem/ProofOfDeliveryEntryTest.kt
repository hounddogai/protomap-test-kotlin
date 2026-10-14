package com.acme.transport.lastmile.proofofdelivery.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryEntryTest {
    @Test
    fun `starts in created`() {
        val item = newProofOfDeliveryEntry("code-422", "label-605")
        assertEquals(ProofOfDeliveryEntryStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to retired`() {
        val item = newProofOfDeliveryEntry("code-422", "label-605")
        try {
            item.moveTo(ProofOfDeliveryEntryStatus.RETIRED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryEntryStatus.RETIRED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newProofOfDeliveryEntry("code-422", "label-605")
        item.status = ProofOfDeliveryEntryStatus.RETIRED
        assertFails("move") { item.moveTo(ProofOfDeliveryEntryStatus.CREATED) }
    }
}
