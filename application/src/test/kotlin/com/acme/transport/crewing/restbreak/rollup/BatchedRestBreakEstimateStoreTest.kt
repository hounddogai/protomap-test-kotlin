package com.acme.transport.crewing.restbreak.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedRestBreakEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedRestBreakEstimateStore()
        val item = newBatchedRestBreakEstimate("code-702", "title-714")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
