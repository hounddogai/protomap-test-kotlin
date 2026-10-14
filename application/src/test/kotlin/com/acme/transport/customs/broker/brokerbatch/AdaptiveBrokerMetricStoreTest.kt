package com.acme.transport.customs.broker.brokerbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveBrokerMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveBrokerMetricStore()
        val item = newAdaptiveBrokerMetric("code-175", "name-43")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
