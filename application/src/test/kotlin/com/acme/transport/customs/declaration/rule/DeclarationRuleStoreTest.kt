package com.acme.transport.customs.declaration.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DeclarationRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDeclarationRuleStore()
        val item = newDeclarationRule("code-22", "name-943")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
