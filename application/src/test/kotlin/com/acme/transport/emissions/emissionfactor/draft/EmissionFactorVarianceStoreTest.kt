package com.acme.transport.emissions.emissionfactor.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EmissionFactorVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEmissionFactorVarianceStore()
        val item = newEmissionFactorVariance("code-18", "name-281")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
