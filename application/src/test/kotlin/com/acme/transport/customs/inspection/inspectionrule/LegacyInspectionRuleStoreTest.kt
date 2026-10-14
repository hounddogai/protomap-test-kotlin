package com.acme.transport.customs.inspection.inspectionrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyInspectionRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyInspectionRuleStore()
        val item = newLegacyInspectionRule("code-908", "name-981")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
