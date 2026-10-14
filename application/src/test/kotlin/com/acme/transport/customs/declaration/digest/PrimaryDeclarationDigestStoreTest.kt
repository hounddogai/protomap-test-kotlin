package com.acme.transport.customs.declaration.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryDeclarationDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryDeclarationDigestStore()
        val item = newPrimaryDeclarationDigest("code-133", "title-116")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
