package com.acme.transport.lastmile.drop.batcheddropnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DropBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDropBatchStore()
        val item = newDropBatch("code-143", "title-771")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
