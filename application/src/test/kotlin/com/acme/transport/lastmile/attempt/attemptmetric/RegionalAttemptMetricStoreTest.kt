package com.acme.transport.lastmile.attempt.attemptmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalAttemptMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalAttemptMetricStore()
        val item = newRegionalAttemptMetric("code-3", "title-728")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
