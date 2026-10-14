package com.acme.transport.linehaul.tender.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultTenderMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultTenderMetricStore()
        val item = newDefaultTenderMetric("code-382", "label-813")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
