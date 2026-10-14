package com.acme.transport.terminal.sortation.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SortationSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySortationSegmentStore()
        val item = newSortationSegment("code-982", "label-107")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
