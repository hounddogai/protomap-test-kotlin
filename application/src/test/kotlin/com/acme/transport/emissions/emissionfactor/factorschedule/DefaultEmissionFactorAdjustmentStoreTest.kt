package com.acme.transport.emissions.emissionfactor.factorschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultEmissionFactorAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultEmissionFactorAdjustmentStore()
        val item = newDefaultEmissionFactorAdjustment("code-873", "name-977")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
