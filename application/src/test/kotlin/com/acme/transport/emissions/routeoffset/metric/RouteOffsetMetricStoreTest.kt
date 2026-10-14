package com.acme.transport.emissions.routeoffset.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RouteOffsetMetricStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRouteOffsetMetricStore()
        val item = newRouteOffsetMetric("code-69", "name-522")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
