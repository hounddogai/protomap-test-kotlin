package com.acme.transport.linehaul.capacity.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CapacitySlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCapacitySlotStore()
        val item = newCapacitySlot("code-880", "label-975")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
