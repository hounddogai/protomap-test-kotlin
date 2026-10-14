package com.acme.transport.network.node.nodedraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyNodePlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyNodePlanStore()
        val item = newLegacyNodePlan("code-365", "name-950")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
