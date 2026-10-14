package com.acme.transport.linehaul.backhaul.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedBackhaulRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedBackhaulRequestStore()
        val item = newBatchedBackhaulRequest("code-595", "label-806")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
