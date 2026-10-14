package com.acme.transport.emissions.emissionfactor.factortarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackEmissionFactorReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackEmissionFactorReservationStore()
        val item = newFallbackEmissionFactorReservation("code-1", "name-801")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
