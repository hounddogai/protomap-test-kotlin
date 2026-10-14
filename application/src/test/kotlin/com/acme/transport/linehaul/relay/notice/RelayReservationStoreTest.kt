package com.acme.transport.linehaul.relay.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RelayReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRelayReservationStore()
        val item = newRelayReservation("code-327", "name-768")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
