package com.acme.transport.network.cutoff.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CutoffBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCutoffBatchStore()
        val item = newCutoffBatch("code-453", "title-67")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
