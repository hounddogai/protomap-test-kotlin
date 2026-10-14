package com.acme.transport.linehaul.capacity.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CapacityGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCapacityGroupStore()
        val item = newCapacityGroup("code-535", "label-196")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
