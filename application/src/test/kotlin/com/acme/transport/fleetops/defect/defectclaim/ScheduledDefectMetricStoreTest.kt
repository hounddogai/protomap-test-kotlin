package com.acme.transport.fleetops.defect.defectclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledDefectMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledDefectMetricStore()
        val item = newScheduledDefectMetric("code-224", "title-547")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
