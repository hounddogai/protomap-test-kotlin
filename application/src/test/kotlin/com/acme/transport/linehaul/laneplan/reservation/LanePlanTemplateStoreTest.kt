package com.acme.transport.linehaul.laneplan.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LanePlanTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLanePlanTemplateStore()
        val item = newLanePlanTemplate("code-8", "label-893")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
