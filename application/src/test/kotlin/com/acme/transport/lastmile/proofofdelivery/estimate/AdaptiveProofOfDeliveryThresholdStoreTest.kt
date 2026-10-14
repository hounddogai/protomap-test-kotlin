package com.acme.transport.lastmile.proofofdelivery.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveProofOfDeliveryThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveProofOfDeliveryThresholdStore()
        val item = newAdaptiveProofOfDeliveryThreshold("code-710", "name-582")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
