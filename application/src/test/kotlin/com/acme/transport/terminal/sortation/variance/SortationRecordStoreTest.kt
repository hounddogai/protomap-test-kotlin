package com.acme.transport.terminal.sortation.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SortationRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySortationRecordStore()
        val item = newSortationRecord("code-490", "label-354")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
