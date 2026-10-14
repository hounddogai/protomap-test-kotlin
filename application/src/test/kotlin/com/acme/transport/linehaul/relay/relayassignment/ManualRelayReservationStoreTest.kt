package com.acme.transport.linehaul.relay.relayassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualRelayReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualRelayReservationStore()
        val item = newManualRelayReservation("code-960", "title-162")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
