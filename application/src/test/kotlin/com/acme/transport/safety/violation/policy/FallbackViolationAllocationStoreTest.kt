package com.acme.transport.safety.violation.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackViolationAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackViolationAllocationStore()
        val item = newFallbackViolationAllocation("code-357", "label-521")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
