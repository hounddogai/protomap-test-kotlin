package com.acme.transport.customs.declaration.declarationbatch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalDeclarationQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalDeclarationQuotaStore()
        val item = newRegionalDeclarationQuota("code-994", "label-527")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
