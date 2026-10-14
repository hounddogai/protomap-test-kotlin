package com.acme.transport.fleetops.defect.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingDefectMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingDefectMetricStore()
        val item = newPendingDefectMetric("code-526", "name-733")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
