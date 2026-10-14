package com.acme.transport.customs.declaration.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DeclarationAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDeclarationAdjustmentStore()
        val item = newDeclarationAdjustment("code-561", "label-734")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
