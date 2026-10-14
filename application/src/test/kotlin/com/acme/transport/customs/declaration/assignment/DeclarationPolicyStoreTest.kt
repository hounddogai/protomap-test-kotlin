package com.acme.transport.customs.declaration.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DeclarationPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDeclarationPolicyStore()
        val item = newDeclarationPolicy("code-961", "label-819")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
