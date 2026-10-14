package com.acme.transport.terminal.sortation.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SortationMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySortationMappingStore()
        val item = newSortationMapping("code-241", "title-340")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
