package com.acme.transport.lastmile.proofofdelivery.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryTemplateTest {
    @Test
    fun `starts in created`() {
        val item = newProofOfDeliveryTemplate("code-431", "label-257")
        assertEquals(ProofOfDeliveryTemplateStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newProofOfDeliveryTemplate("code-431", "label-257")
        try {
            item.moveTo(ProofOfDeliveryTemplateStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryTemplateStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newProofOfDeliveryTemplate("code-431", "label-257")
        item.status = ProofOfDeliveryTemplateStatus.RETIRED
        assertFails("move") { item.moveTo(ProofOfDeliveryTemplateStatus.CREATED) }
    }
}
