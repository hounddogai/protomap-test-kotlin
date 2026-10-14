package com.acme.transport.customs.declaration.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredDeclarationBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredDeclarationBaselineStore()
        val item = newTieredDeclarationBaseline("code-627", "label-682")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
