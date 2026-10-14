package com.acme.transport.safety.violation.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ViolationPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryViolationPolicyStore()
        val item = newViolationPolicy("code-504", "title-152")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
