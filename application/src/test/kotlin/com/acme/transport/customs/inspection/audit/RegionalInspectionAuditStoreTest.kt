package com.acme.transport.customs.inspection.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalInspectionAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalInspectionAuditStore()
        val item = newRegionalInspectionAudit("code-751", "name-379")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
