package com.acme.transport.customs.declaration.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedDeclarationManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedDeclarationManifestStore()
        val item = newBatchedDeclarationManifest("code-66", "name-819")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
