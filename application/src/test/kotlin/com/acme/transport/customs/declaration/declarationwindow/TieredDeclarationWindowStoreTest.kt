package com.acme.transport.customs.declaration.declarationwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredDeclarationWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredDeclarationWindowStore()
        val item = newTieredDeclarationWindow("code-460", "title-77")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
