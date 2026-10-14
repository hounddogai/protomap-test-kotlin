package com.acme.transport.terminal.sortation.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictSortationBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictSortationBatchStore()
        val item = newStrictSortationBatch("code-811", "title-985")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
