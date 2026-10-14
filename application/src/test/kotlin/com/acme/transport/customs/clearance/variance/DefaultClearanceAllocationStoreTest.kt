package com.acme.transport.customs.clearance.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultClearanceAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultClearanceAllocationStore()
        val item = newDefaultClearanceAllocation("code-312", "label-948")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
