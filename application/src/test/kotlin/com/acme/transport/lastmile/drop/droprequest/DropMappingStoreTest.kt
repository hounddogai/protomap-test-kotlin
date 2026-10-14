package com.acme.transport.lastmile.drop.droprequest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DropMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDropMappingStore()
        val item = newDropMapping("code-472", "label-595")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
