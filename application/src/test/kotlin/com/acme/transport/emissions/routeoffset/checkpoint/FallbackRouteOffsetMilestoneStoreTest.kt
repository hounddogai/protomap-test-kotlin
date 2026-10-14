package com.acme.transport.emissions.routeoffset.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackRouteOffsetMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackRouteOffsetMilestoneStore()
        val item = newFallbackRouteOffsetMilestone("code-5", "title-326")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
