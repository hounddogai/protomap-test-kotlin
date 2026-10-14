package com.acme.transport.customs.declaration.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DeclarationQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDeclarationQuotaStore()
        val item = newDeclarationQuota("code-493", "label-687")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
