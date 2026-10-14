package com.acme.transport.fleetops.inspection.inspectionslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionRuleStore()
        val item = newInspectionRule("code-535", "label-796")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
