package com.acme.transport.emissions.routeoffset.offsetrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedRouteOffsetTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedRouteOffsetTemplateStore()
        val item = newWeightedRouteOffsetTemplate("code-252", "title-672")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
