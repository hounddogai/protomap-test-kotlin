package com.acme.transport.network.transittime.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualTransitTimeSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualTransitTimeSlotStore()
        val item = newManualTransitTimeSlot("code-509", "name-723")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
