package com.acme.transport.linehaul.capacity.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientCapacityRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientCapacityRollupStore()
        val item = newLenientCapacityRollup("code-55", "title-323")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
