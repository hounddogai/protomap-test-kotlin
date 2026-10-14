package com.acme.transport.fleetops.tire.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingTireReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingTireReservationStore()
        val item = newRollingTireReservation("code-726", "title-950")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
