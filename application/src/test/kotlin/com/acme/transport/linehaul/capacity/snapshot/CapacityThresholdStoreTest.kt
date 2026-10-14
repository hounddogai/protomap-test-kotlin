package com.acme.transport.linehaul.capacity.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CapacityThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCapacityThresholdStore()
        val item = newCapacityThreshold("code-586", "name-460")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
