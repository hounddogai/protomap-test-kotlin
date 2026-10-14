package com.acme.transport.lastmile.drop.dropbundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredDropTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredDropTargetStore()
        val item = newTieredDropTarget("code-281", "title-98")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
