package com.acme.transport.emissions.routeoffset.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientRouteOffsetCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientRouteOffsetCheckpointStore()
        val item = newLenientRouteOffsetCheckpoint("code-793", "title-639")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
