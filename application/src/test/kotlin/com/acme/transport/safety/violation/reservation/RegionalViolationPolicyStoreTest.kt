package com.acme.transport.safety.violation.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalViolationPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalViolationPolicyStore()
        val item = newRegionalViolationPolicy("code-256", "label-236")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
