package com.acme.transport.crewing.swap.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SwapVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySwapVarianceStore()
        val item = newSwapVariance("code-962", "title-775")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
