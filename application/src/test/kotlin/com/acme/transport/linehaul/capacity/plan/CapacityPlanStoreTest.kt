package com.acme.transport.linehaul.capacity.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CapacityPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCapacityPlanStore()
        val item = newCapacityPlan("code-488", "label-2")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
