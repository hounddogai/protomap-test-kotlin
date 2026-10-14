package com.acme.transport.emissions.emissionfactor.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveEmissionFactorMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveEmissionFactorMappingStore()
        val item = newAdaptiveEmissionFactorMapping("code-795", "name-784")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
