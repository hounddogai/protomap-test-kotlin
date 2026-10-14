package com.acme.transport.safety.violation.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ViolationAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryViolationAdjustmentStore()
        val item = newViolationAdjustment("code-528", "name-442")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
