package com.acme.transport.customs.declaration.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedDeclarationCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedDeclarationCheckpointStore()
        val item = newBatchedDeclarationCheckpoint("code-817", "name-806")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
