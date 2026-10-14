package com.acme.transport.crewing.swap.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SwapMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySwapMetricStore()
        val item = newSwapMetric("code-663", "title-639")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
