package com.acme.transport.linehaul.backhaul.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedBackhaulTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedBackhaulTargetStore()
        val item = newBatchedBackhaulTarget("code-480", "label-855")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
