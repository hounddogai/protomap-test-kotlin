package com.acme.transport.crewing.swap.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedSwapVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedSwapVarianceStore()
        val item = newBatchedSwapVariance("code-395", "name-538")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
