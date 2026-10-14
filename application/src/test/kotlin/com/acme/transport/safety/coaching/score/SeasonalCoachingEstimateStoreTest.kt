package com.acme.transport.safety.coaching.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalCoachingEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalCoachingEstimateStore()
        val item = newSeasonalCoachingEstimate("code-479", "name-225")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
