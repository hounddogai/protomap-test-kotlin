package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class TieredProofOfDeliveryManifestTest {
    @Test
    fun `starts in draft`() {
        val item = newTieredProofOfDeliveryManifest("code-856", "name-734")
        assertEquals(TieredProofOfDeliveryManifestStatus.DRAFT, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to suspended`() {
        val item = newTieredProofOfDeliveryManifest("code-856", "name-734")
        try {
            item.moveTo(TieredProofOfDeliveryManifestStatus.SUSPENDED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(TieredProofOfDeliveryManifestStatus.SUSPENDED, item.status, "status")
    }

    @Test
    fun `rejects moves from closed`() {
        val item = newTieredProofOfDeliveryManifest("code-856", "name-734")
        item.status = TieredProofOfDeliveryManifestStatus.CLOSED
        assertFails("move") { item.moveTo(TieredProofOfDeliveryManifestStatus.DRAFT) }
    }
}
