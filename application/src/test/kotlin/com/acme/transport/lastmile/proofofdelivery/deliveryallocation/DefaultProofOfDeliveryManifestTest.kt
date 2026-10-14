package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertTrue
import kotlin.test.fail

class DefaultProofOfDeliveryManifestTest {
    @Test
    fun `starts in proposed`() {
        val item = newDefaultProofOfDeliveryManifest("code-659", "label-80")
        assertEquals(DefaultProofOfDeliveryManifestStatus.PROPOSED, item.status, "status")
        assertTrue(!item.isTerminal(), "terminal")
    }

    @Test
    fun `moves to approved`() {
        val item = newDefaultProofOfDeliveryManifest("code-659", "label-80")
        try {
            item.moveTo(DefaultProofOfDeliveryManifestStatus.APPROVED)
        } catch (failure: Exception) {
            fail("missing result")
        }
        assertEquals(DefaultProofOfDeliveryManifestStatus.APPROVED, item.status, "status")
    }

    @Test
    fun `rejects moves from withdrawn`() {
        val item = newDefaultProofOfDeliveryManifest("code-659", "label-80")
        item.status = DefaultProofOfDeliveryManifestStatus.WITHDRAWN
        assertFails("move") { item.moveTo(DefaultProofOfDeliveryManifestStatus.PROPOSED) }
    }
}
