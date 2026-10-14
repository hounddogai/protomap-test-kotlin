package com.acme.transport.lastmile.drop.dropentry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackDropMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackDropMetricStore()
        val item = newFallbackDropMetric("code-712", "label-189")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
