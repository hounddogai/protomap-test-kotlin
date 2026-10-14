package com.acme.transport.emissions.routeoffset.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RouteOffsetCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRouteOffsetCheckpointStore()
        val item = newRouteOffsetCheckpoint("code-382", "name-390")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
