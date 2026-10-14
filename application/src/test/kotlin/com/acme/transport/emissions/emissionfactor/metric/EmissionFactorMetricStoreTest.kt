package com.acme.transport.emissions.emissionfactor.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EmissionFactorMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEmissionFactorMetricStore()
        val item = newEmissionFactorMetric("code-845", "label-41")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
