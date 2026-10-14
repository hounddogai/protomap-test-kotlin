package com.acme.transport.customs.declaration.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DeclarationWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDeclarationWindowStore()
        val item = newDeclarationWindow("code-717", "name-910")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
