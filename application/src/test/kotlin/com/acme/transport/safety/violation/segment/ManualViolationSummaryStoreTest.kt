package com.acme.transport.safety.violation.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualViolationSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualViolationSummaryStore()
        val item = newManualViolationSummary("code-76", "name-117")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
