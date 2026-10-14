package com.acme.transport.emissions.routeoffset.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RouteOffsetMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRouteOffsetMilestoneStore()
        val item = newRouteOffsetMilestone("code-958", "title-562")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
