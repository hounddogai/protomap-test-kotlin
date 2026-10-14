package com.acme.transport.emissions.evcharge.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingEvChargeMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingEvChargeMetricStore()
        val item = newRollingEvChargeMetric("code-57", "label-639")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
