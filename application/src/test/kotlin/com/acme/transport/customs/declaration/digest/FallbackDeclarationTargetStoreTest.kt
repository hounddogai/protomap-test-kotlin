package com.acme.transport.customs.declaration.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackDeclarationTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackDeclarationTargetStore()
        val item = newFallbackDeclarationTarget("code-779", "title-619")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
