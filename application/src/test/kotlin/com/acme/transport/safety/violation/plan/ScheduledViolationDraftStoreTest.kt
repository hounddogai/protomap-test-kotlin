package com.acme.transport.safety.violation.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledViolationDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledViolationDraftStore()
        val item = newScheduledViolationDraft("code-572", "name-490")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
