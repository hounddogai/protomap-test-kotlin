package com.acme.transport.emissions.idling.regionalidlingmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalIdlingReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalIdlingReservationStore()
        val item = newRegionalIdlingReservation("code-664", "label-998")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
