package com.acme.transport.customs.declaration.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalDeclarationAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalDeclarationAuditStore()
        val item = newRegionalDeclarationAudit("code-15", "label-92")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
