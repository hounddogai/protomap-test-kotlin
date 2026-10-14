package com.acme.transport.crewing.roster.regionalrostersummary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredRosterPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredRosterPlanStore()
        val item = newTieredRosterPlan("code-373", "name-924")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
