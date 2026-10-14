package com.acme.transport.lastmile.parcel.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualParcelMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualParcelMetricStore()
        val item = newManualParcelMetric("code-985", "name-163")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
