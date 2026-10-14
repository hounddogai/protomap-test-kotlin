package com.acme.transport.emissions.routeoffset.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredRouteOffsetMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredRouteOffsetMilestoneStore()
        val item = newTieredRouteOffsetMilestone("code-583", "title-650")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
