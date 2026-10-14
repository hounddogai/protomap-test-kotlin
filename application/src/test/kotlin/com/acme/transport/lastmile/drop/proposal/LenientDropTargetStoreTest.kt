package com.acme.transport.lastmile.drop.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientDropTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientDropTargetStore()
        val item = newLenientDropTarget("code-923", "title-244")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
