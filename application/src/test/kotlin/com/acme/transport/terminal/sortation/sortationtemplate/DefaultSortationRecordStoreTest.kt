package com.acme.transport.terminal.sortation.sortationtemplate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultSortationRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultSortationRecordStore()
        val item = newDefaultSortationRecord("code-341", "label-438")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
