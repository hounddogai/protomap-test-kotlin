package com.acme.transport.lastmile.proofofdelivery.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedProofOfDeliveryThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedProofOfDeliveryThresholdStore()
        val item = newWeightedProofOfDeliveryThreshold("code-674", "title-853")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
