package com.acme.transport.customs.declaration.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedDeclarationSnapshotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedDeclarationSnapshotStore()
        val item = newBatchedDeclarationSnapshot("code-221", "label-553")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
