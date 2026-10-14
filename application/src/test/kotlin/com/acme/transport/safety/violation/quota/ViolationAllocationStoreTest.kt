package com.acme.transport.safety.violation.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ViolationAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryViolationAllocationStore()
        val item = newViolationAllocation("code-562", "label-131")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
