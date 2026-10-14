package com.acme.transport.emissions.routeoffset.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RouteOffsetThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRouteOffsetThresholdStore()
        val item = newRouteOffsetThreshold("code-274", "label-479")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
