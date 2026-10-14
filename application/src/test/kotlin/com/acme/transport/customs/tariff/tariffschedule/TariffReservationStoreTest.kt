package com.acme.transport.customs.tariff.tariffschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TariffReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTariffReservationStore()
        val item = newTariffReservation("code-438", "name-841")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
