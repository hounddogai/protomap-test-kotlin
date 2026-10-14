package com.acme.transport.crewing.roster.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalRosterEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalRosterEstimateStore()
        val item = newSeasonalRosterEstimate("code-53", "title-617")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
