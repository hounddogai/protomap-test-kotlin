package com.acme.transport.network.node.nodedraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientNodeMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientNodeMappingStore()
        val item = newLenientNodeMapping("code-111", "title-542")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
