package com.acme.transport.crewing.swap.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalSwapMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalSwapMetricStore()
        val item = newGlobalSwapMetric("code-533", "title-933")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
