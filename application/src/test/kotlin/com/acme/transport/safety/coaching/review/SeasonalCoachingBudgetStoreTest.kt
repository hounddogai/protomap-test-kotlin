package com.acme.transport.safety.coaching.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalCoachingBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalCoachingBudgetStore()
        val item = newSeasonalCoachingBudget("code-142", "label-653")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
