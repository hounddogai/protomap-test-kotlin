package com.acme.transport.crewing.qualification.qualificationmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredQualificationMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredQualificationMetricStore()
        val item = newTieredQualificationMetric("code-381", "title-351")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
