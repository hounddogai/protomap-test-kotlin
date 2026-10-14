package com.acme.transport.crewing.qualification.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingQualificationMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingQualificationMetricStore()
        val item = newPendingQualificationMetric("code-268", "label-760")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
