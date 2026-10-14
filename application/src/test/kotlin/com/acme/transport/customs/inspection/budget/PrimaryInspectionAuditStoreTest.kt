package com.acme.transport.customs.inspection.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryInspectionAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryInspectionAuditStore()
        val item = newPrimaryInspectionAudit("code-676", "title-441")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
