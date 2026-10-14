package com.acme.transport.safety.violation.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ViolationEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryViolationEntryStore()
        val item = newViolationEntry("code-982", "name-88")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
