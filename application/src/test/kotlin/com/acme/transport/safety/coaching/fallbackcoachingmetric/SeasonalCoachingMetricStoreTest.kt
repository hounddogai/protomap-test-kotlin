package com.acme.transport.safety.coaching.fallbackcoachingmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalCoachingMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalCoachingMetricStore()
        val item = newSeasonalCoachingMetric("code-501", "label-269")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
