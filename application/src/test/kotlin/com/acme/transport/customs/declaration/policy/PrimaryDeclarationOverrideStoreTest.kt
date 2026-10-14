package com.acme.transport.customs.declaration.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryDeclarationOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryDeclarationOverrideStore()
        val item = newPrimaryDeclarationOverride("code-904", "title-67")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
