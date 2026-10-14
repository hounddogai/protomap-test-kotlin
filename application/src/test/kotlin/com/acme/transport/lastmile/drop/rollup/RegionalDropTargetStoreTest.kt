package com.acme.transport.lastmile.drop.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalDropTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalDropTargetStore()
        val item = newRegionalDropTarget("code-825", "title-683")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
