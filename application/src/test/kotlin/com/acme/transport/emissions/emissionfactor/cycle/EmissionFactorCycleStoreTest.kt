package com.acme.transport.emissions.emissionfactor.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EmissionFactorCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEmissionFactorCycleStore()
        val item = newEmissionFactorCycle("code-696", "name-33")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
