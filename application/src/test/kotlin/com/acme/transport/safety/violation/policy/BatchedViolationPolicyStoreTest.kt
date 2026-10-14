package com.acme.transport.safety.violation.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedViolationPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedViolationPolicyStore()
        val item = newBatchedViolationPolicy("code-392", "label-979")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
