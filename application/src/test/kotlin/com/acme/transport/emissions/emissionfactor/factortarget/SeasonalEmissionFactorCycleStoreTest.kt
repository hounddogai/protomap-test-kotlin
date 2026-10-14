package com.acme.transport.emissions.emissionfactor.factortarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalEmissionFactorCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalEmissionFactorCycleStore()
        val item = newSeasonalEmissionFactorCycle("code-229", "label-172")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
