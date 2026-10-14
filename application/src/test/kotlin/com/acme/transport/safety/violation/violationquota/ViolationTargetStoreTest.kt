package com.acme.transport.safety.violation.violationquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ViolationTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryViolationTargetStore()
        val item = newViolationTarget("code-77", "label-181")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
