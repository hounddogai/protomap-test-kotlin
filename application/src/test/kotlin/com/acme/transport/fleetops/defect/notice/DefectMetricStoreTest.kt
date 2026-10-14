package com.acme.transport.fleetops.defect.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefectMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefectMetricStore()
        val item = newDefectMetric("code-408", "name-609")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
