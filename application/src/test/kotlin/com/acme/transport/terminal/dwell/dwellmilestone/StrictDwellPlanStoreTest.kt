package com.acme.transport.terminal.dwell.dwellmilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictDwellPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictDwellPlanStore()
        val item = newStrictDwellPlan("code-879", "label-663")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
