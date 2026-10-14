package com.acme.transport.linehaul.capacity.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredCapacityGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredCapacityGroupStore()
        val item = newTieredCapacityGroup("code-985", "name-214")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
