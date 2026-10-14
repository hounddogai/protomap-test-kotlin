package com.acme.transport.crewing.roster.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingRosterPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingRosterPlanStore()
        val item = newPendingRosterPlan("code-282", "name-172")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
