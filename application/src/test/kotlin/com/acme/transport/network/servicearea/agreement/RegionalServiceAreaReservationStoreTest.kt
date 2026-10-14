package com.acme.transport.network.servicearea.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalServiceAreaReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalServiceAreaReservationStore()
        val item = newRegionalServiceAreaReservation("code-436", "title-528")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
