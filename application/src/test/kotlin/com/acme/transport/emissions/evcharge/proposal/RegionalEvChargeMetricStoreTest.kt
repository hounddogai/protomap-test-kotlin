package com.acme.transport.emissions.evcharge.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalEvChargeMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalEvChargeMetricStore()
        val item = newRegionalEvChargeMetric("code-419", "title-369")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
