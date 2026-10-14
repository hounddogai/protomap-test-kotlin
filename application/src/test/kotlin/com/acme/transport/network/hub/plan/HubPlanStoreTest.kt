package com.acme.transport.network.hub.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HubPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHubPlanStore()
        val item = newHubPlan("code-650", "name-229")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
