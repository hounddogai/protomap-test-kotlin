package com.acme.transport.emissions.emissionfactor.factorsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedEmissionFactorBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedEmissionFactorBatchStore()
        val item = newWeightedEmissionFactorBatch("code-935", "name-451")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
