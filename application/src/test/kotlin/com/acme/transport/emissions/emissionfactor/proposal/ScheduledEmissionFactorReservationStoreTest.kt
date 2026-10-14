package com.acme.transport.emissions.emissionfactor.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledEmissionFactorReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledEmissionFactorReservationStore()
        val item = newScheduledEmissionFactorReservation("code-334", "title-921")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
