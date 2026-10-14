package com.acme.transport.safety.coaching.coachingsnapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CoachingForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCoachingForecastStore()
        val item = newCoachingForecast("code-737", "title-634")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
