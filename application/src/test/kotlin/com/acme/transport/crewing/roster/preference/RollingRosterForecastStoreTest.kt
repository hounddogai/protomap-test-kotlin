package com.acme.transport.crewing.roster.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingRosterForecastStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingRosterForecastStore()
        val item = newRollingRosterForecast("code-468", "title-996")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
