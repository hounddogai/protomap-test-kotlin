package com.acme.transport.crewing.restbreak.breakentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedRestBreakThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedRestBreakThresholdStore()
        val item = newBatchedRestBreakThreshold("code-581", "label-395")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
