package com.acme.transport.network.cutoff.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredCutoffMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredCutoffMetricStore()
        val item = newTieredCutoffMetric("code-625", "label-934")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
