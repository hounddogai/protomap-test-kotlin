package com.acme.transport.emissions.routeoffset.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalRouteOffsetAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalRouteOffsetAdjustmentStore()
        val item = newSeasonalRouteOffsetAdjustment("code-511", "name-168")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
