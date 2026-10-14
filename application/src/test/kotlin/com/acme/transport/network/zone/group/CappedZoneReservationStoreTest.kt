package com.acme.transport.network.zone.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedZoneReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedZoneReservationStore()
        val item = newCappedZoneReservation("code-730", "label-295")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
