package com.acme.transport.linehaul.laneplan.planbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveLanePlanTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveLanePlanTemplateStore()
        val item = newAdaptiveLanePlanTemplate("code-653", "name-405")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
