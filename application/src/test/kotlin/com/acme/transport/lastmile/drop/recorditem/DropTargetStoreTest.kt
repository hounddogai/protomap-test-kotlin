package com.acme.transport.lastmile.drop.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DropTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDropTargetStore()
        val item = newDropTarget("code-209", "title-886")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
