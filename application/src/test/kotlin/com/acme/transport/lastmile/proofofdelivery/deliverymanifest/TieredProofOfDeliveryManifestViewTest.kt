package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredProofOfDeliveryManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredProofOfDeliveryManifest("code-856", "name-734")
        val view = toTieredProofOfDeliveryManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredProofOfDeliveryManifestViews(mutableListOf<TieredProofOfDeliveryManifest>(item, item)).size, "views")
    }
}
