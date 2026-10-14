package com.acme.transport.terminal.sortation.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SortationOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySortationOverrideStore()
        val item = newSortationOverride("code-775", "label-821")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
