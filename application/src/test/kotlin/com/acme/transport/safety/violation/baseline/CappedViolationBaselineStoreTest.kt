package com.acme.transport.safety.violation.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedViolationBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedViolationBaselineStore()
        val item = newCappedViolationBaseline("code-659", "name-781")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
