package com.acme.transport.emissions.routeoffset.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledRouteOffsetCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledRouteOffsetCheckpointStore()
        val item = newScheduledRouteOffsetCheckpoint("code-474", "name-407")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
