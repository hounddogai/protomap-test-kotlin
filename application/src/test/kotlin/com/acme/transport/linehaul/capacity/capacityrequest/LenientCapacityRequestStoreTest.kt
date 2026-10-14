package com.acme.transport.linehaul.capacity.capacityrequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientCapacityRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientCapacityRequestStore()
        val item = newLenientCapacityRequest("code-87", "title-401")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
