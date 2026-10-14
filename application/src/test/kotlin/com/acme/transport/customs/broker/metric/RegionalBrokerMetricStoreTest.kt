package com.acme.transport.customs.broker.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalBrokerMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalBrokerMetricStore()
        val item = newRegionalBrokerMetric("code-516", "name-933")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
