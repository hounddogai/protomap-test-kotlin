package com.acme.transport.safety.coaching.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultCoachingMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultCoachingMetricStore()
        val item = newDefaultCoachingMetric("code-656", "name-798")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
