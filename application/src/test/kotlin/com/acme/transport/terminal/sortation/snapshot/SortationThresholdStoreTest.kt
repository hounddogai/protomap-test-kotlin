package com.acme.transport.terminal.sortation.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SortationThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySortationThresholdStore()
        val item = newSortationThreshold("code-814", "label-462")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
