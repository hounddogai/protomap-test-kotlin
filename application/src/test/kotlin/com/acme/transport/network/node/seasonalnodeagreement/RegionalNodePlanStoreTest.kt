package com.acme.transport.network.node.seasonalnodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalNodePlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalNodePlanStore()
        val item = newRegionalNodePlan("code-695", "title-666")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
