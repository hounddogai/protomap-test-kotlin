package com.acme.transport.emissions.emissionfactor.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedEmissionFactorThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedEmissionFactorThresholdStore()
        val item = newWeightedEmissionFactorThreshold("code-958", "title-933")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
