package com.acme.transport.customs.declaration.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DeclarationDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDeclarationDigestStore()
        val item = newDeclarationDigest("code-349", "name-930")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
