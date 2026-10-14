package com.acme.transport.customs.declaration.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalDeclarationAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalDeclarationAuditStore()
        val item = newGlobalDeclarationAudit("code-698", "name-105")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
