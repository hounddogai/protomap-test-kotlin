package com.acme.transport.customs.declaration.declarationmanifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackDeclarationAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackDeclarationAdjustmentStore()
        val item = newFallbackDeclarationAdjustment("code-662", "name-600")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
