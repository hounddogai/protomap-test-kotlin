package com.acme.transport.terminal.sortation.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SortationRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySortationRollupStore()
        val item = newSortationRollup("code-71", "title-204")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
