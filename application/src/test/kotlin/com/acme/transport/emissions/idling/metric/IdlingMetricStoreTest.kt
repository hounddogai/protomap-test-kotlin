package com.acme.transport.emissions.idling.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IdlingMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIdlingMetricStore()
        val item = newIdlingMetric("code-783", "title-935")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
