package com.acme.transport.terminal.traileryard.yardforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalTrailerYardOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalTrailerYardOverrideStore()
        val item = newSeasonalTrailerYardOverride("code-339", "name-578")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
