package com.acme.transport.emissions.emissionfactor.rollingfactorsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalEmissionFactorExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalEmissionFactorExceptionStore()
        val item = newSeasonalEmissionFactorException("code-156", "label-341")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
