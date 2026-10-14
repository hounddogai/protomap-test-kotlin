package com.acme.transport.customs.declaration.declarationwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredDeclarationScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredDeclarationScoreStore()
        val item = newTieredDeclarationScore("code-650", "label-48")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
