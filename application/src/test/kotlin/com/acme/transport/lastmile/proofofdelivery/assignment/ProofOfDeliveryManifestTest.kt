package com.acme.transport.lastmile.proofofdelivery.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class ProofOfDeliveryManifestTest {
    @Test
    fun `starts in created`() {
        val item = newProofOfDeliveryManifest("code-59", "label-979")
        assertEquals(ProofOfDeliveryManifestStatus.CREATED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to validated`() {
        val item = newProofOfDeliveryManifest("code-59", "label-979")
        try {
            item.moveTo(ProofOfDeliveryManifestStatus.VALIDATED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(ProofOfDeliveryManifestStatus.VALIDATED, item.status, "status")
    }

    @Test
    fun `rejects moves from retired`() {
        val item = newProofOfDeliveryManifest("code-59", "label-979")
        item.status = ProofOfDeliveryManifestStatus.RETIRED
        assertFails("move") { item.moveTo(ProofOfDeliveryManifestStatus.CREATED) }
    }
}
