package com.acme.transport.emissions.routeoffset.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RouteOffsetSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRouteOffsetSummaryStore()
        val item = newRouteOffsetSummary("code-206", "title-33")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
