package com.acme.transport.safety.incident.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingIncidentPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingIncidentPlanStore()
        val item = newRollingIncidentPlan("code-98", "name-136")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
