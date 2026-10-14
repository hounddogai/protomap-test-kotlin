package com.acme.transport.customs.declaration.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DeclarationEstimateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDeclarationEstimateStore()
        val item = newDeclarationEstimate("code-855", "title-335")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
