package com.acme.transport.emissions.emissionfactor.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledEmissionFactorMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledEmissionFactorMetricStore()
        val item = newScheduledEmissionFactorMetric("code-531", "label-211")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
