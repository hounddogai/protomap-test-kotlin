package com.acme.transport.lastmile.drop.dropdraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DropOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDropOverrideStore()
        val item = newDropOverride("code-584", "label-468")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
