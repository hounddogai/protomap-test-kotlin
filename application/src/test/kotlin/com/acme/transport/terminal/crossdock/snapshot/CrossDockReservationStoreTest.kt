package com.acme.transport.terminal.crossdock.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CrossDockReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCrossDockReservationStore()
        val item = newCrossDockReservation("code-4", "title-581")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
