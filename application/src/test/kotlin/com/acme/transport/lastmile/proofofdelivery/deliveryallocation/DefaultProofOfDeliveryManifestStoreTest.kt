package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultProofOfDeliveryManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultProofOfDeliveryManifestStore()
        val item = newDefaultProofOfDeliveryManifest("code-659", "label-80")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
