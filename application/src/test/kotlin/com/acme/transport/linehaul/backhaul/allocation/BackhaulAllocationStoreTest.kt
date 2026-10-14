package com.acme.transport.linehaul.backhaul.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BackhaulAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBackhaulAllocationStore()
        val item = newBackhaulAllocation("code-624", "label-124")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
