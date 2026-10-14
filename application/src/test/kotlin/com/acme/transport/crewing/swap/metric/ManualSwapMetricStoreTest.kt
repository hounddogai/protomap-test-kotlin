package com.acme.transport.crewing.swap.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualSwapMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualSwapMetricStore()
        val item = newManualSwapMetric("code-21", "label-85")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
