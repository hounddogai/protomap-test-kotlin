package com.acme.transport.customs.tariff.tariffdigest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveTariffMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveTariffMetricStore()
        val item = newAdaptiveTariffMetric("code-99", "name-448")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
