package com.acme.transport.network.cutoff.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryCutoffBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryCutoffBatchStore()
        val item = newPrimaryCutoffBatch("code-90", "name-376")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
