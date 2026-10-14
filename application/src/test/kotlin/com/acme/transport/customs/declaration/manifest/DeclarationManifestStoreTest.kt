package com.acme.transport.customs.declaration.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DeclarationManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDeclarationManifestStore()
        val item = newDeclarationManifest("code-550", "title-890")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
