package com.acme.transport.emissions.idling.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientIdlingSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientIdlingSlotStore()
        val item = newLenientIdlingSlot("code-183", "label-371")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
