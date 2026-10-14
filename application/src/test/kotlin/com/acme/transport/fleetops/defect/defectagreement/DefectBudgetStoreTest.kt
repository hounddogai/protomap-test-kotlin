package com.acme.transport.fleetops.defect.defectagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefectBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefectBudgetStore()
        val item = newDefectBudget("code-360", "label-649")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
