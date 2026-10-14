package com.acme.transport.linehaul.capacity.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CapacityProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCapacityProfileStore()
        val item = newCapacityProfile("code-758", "name-772")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
