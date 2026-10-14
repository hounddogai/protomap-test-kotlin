package com.acme.transport.emissions.routeoffset.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryRouteOffsetBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryRouteOffsetBudgetStore()
        val item = newPrimaryRouteOffsetBudget("code-598", "label-772")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
