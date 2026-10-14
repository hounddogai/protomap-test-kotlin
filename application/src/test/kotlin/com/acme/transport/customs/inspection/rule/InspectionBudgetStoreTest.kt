package com.acme.transport.customs.inspection.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionBudgetStore()
        val item = newInspectionBudget("code-853", "label-517")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
