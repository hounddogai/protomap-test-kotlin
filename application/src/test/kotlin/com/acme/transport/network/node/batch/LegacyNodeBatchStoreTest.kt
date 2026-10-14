package com.acme.transport.network.node.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyNodeBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyNodeBatchStore()
        val item = newLegacyNodeBatch("code-231", "title-801")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
