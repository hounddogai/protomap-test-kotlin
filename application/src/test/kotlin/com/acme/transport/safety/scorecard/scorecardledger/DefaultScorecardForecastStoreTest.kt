package com.acme.transport.safety.scorecard.scorecardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultScorecardForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultScorecardForecastStore()
        val item = newDefaultScorecardForecast("code-786", "name-524")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
