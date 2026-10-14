package com.acme.transport.lastmile.attempt.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultAttemptMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultAttemptMetricStore()
        val item = newDefaultAttemptMetric("code-403", "name-240")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
