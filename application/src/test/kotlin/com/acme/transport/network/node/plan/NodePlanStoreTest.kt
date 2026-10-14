package com.acme.transport.network.node.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NodePlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNodePlanStore()
        val item = newNodePlan("code-51", "name-302")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
