package com.acme.transport.lastmile.parcel.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ParcelReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryParcelReservationStore()
        val item = newParcelReservation("code-713", "title-147")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
