package com.acme.transport.emissions.routeoffset.offsetassignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RouteOffsetBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRouteOffsetBudgetStore()
        val item = newRouteOffsetBudget("code-608", "label-9")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
