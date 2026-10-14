package com.acme.transport.terminal.sortation.sortationtier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SortationAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySortationAuditStore()
        val item = newSortationAudit("code-763", "name-252")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
