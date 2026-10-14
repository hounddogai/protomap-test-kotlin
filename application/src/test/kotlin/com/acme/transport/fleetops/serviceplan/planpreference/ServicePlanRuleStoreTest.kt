package com.acme.transport.fleetops.serviceplan.planpreference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServicePlanRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServicePlanRuleStore()
        val item = newServicePlanRule("code-41", "label-731")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
