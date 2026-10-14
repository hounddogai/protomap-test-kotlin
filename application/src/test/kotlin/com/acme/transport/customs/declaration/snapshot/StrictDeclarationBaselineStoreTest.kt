package com.acme.transport.customs.declaration.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictDeclarationBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictDeclarationBaselineStore()
        val item = newStrictDeclarationBaseline("code-667", "name-708")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
