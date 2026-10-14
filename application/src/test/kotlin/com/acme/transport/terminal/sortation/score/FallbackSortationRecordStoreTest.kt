package com.acme.transport.terminal.sortation.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackSortationRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackSortationRecordStore()
        val item = newFallbackSortationRecord("code-251", "label-854")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
