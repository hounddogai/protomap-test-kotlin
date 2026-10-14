package com.acme.transport.terminal.sortation.sortationvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedSortationOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedSortationOverrideStore()
        val item = newBatchedSortationOverride("code-901", "label-12")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
