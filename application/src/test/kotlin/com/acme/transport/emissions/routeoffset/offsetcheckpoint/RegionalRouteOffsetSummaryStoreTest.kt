package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalRouteOffsetSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalRouteOffsetSummaryStore()
        val item = newRegionalRouteOffsetSummary("code-180", "label-827")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
