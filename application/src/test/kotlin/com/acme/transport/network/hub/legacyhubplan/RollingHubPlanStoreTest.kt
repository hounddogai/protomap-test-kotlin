package com.acme.transport.network.hub.legacyhubplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingHubPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingHubPlanStore()
        val item = newRollingHubPlan("code-986", "label-661")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
