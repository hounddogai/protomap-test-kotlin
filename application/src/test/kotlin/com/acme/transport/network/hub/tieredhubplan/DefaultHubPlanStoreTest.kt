package com.acme.transport.network.hub.tieredhubplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultHubPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultHubPlanStore()
        val item = newDefaultHubPlan("code-799", "label-712")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
