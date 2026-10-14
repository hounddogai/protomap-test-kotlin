package com.acme.transport.lastmile.proofofdelivery.deliverymanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredProofOfDeliveryManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredProofOfDeliveryManifestStore()
        val item = newTieredProofOfDeliveryManifest("code-856", "name-734")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
