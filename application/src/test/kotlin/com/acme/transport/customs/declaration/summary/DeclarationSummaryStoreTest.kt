package com.acme.transport.customs.declaration.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DeclarationSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDeclarationSummaryStore()
        val item = newDeclarationSummary("code-772", "name-889")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
