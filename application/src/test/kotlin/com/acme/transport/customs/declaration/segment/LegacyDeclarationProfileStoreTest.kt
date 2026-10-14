package com.acme.transport.customs.declaration.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyDeclarationProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyDeclarationProfileStore()
        val item = newLegacyDeclarationProfile("code-56", "label-455")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
