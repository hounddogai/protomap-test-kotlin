package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultProofOfDeliveryManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultProofOfDeliveryManifest("code-659", "label-80")
        val view = toDefaultProofOfDeliveryManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultProofOfDeliveryManifestViews(mutableListOf<DefaultProofOfDeliveryManifest>(item, item)).size, "views")
    }
}
