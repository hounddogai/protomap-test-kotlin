package com.acme.transport.customs.inspection.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionRuleStore()
        val item = newInspectionRule("code-675", "label-476")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
