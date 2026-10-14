package com.acme.transport.emissions.routeoffset.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RouteOffsetSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRouteOffsetSegmentStore()
        val item = newRouteOffsetSegment("code-454", "name-435")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
