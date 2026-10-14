package com.acme.transport.emissions.emissionfactor.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveEmissionFactorVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveEmissionFactorVarianceStore()
        val item = newAdaptiveEmissionFactorVariance("code-124", "label-278")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
