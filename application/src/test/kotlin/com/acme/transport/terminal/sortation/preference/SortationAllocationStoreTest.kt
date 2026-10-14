package com.acme.transport.terminal.sortation.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SortationAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySortationAllocationStore()
        val item = newSortationAllocation("code-176", "name-204")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
