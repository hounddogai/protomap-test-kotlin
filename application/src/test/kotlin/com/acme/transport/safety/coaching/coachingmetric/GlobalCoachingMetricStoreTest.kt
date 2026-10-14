package com.acme.transport.safety.coaching.coachingmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalCoachingMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalCoachingMetricStore()
        val item = newGlobalCoachingMetric("code-34", "name-316")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
