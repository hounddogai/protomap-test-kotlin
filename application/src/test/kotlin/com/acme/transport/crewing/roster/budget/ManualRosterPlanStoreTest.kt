package com.acme.transport.crewing.roster.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualRosterPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualRosterPlanStore()
        val item = newManualRosterPlan("code-72", "title-276")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
