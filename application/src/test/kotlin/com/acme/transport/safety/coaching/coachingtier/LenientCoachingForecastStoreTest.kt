package com.acme.transport.safety.coaching.coachingtier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientCoachingForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientCoachingForecastStore()
        val item = newLenientCoachingForecast("code-617", "title-371")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
