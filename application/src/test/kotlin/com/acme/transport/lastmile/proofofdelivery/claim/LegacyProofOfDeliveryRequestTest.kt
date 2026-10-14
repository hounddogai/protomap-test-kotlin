package com.acme.transport.lastmile.proofofdelivery.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class LegacyProofOfDeliveryRequestTest {
    @Test
    fun `starts in draft`() {
        val item = newLegacyProofOfDeliveryRequest("code-489", "title-869")
        assertEquals(LegacyProofOfDeliveryRequestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to closed`() {
        val item = newLegacyProofOfDeliveryRequest("code-489", "title-869")
        try {
            item.moveTo(LegacyProofOfDeliveryRequestStatus.CLOSED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(LegacyProofOfDeliveryRequestStatus.CLOSED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newLegacyProofOfDeliveryRequest("code-489", "title-869")
        item.status = LegacyProofOfDeliveryRequestStatus.CLOSED
        assertFails("move") { item.moveTo(LegacyProofOfDeliveryRequestStatus.DRAFT) }
    }
}
