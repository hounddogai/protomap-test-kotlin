package com.acme.transport.lastmile.proofofdelivery.deliveryclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ManualProofOfDeliveryClaimTest {
    @Test
    fun `starts in proposed`() {
        val item = newManualProofOfDeliveryClaim("code-168", "label-549")
        assertEquals(ManualProofOfDeliveryClaimStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newManualProofOfDeliveryClaim("code-168", "label-549")
        try {
            item.moveTo(ManualProofOfDeliveryClaimStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ManualProofOfDeliveryClaimStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newManualProofOfDeliveryClaim("code-168", "label-549")
        item.status = ManualProofOfDeliveryClaimStatus.WITHDRAWN
        assertFails("move") { item.moveTo(ManualProofOfDeliveryClaimStatus.PROPOSED) }
    }
}
