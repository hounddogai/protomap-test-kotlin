package com.acme.transport.crewing.roster.rosterforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientRosterForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientRosterForecastStore()
        val item = newLenientRosterForecast("code-226", "name-550")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
