package com.acme.transport.safety.violation.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ViolationSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryViolationSummaryStore()
        val item = newViolationSummary("code-790", "name-380")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
