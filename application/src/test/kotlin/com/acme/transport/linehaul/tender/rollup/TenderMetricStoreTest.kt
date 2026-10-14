package com.acme.transport.linehaul.tender.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TenderMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTenderMetricStore()
        val item = newTenderMetric("code-974", "label-651")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
