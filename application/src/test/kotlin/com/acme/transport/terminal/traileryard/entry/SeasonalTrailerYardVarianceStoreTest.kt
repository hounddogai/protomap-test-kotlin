package com.acme.transport.terminal.traileryard.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalTrailerYardVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalTrailerYardVarianceStore()
        val item = newSeasonalTrailerYardVariance("code-878", "name-676")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
