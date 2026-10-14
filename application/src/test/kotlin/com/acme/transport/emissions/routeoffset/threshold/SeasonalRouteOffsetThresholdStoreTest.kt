package com.acme.transport.emissions.routeoffset.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalRouteOffsetThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalRouteOffsetThresholdStore()
        val item = newSeasonalRouteOffsetThreshold("code-777", "title-129")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
