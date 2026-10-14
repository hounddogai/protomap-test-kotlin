package com.acme.transport.customs.tariff.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingTariffMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingTariffMetricStore()
        val item = newPendingTariffMetric("code-738", "name-407")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
