package com.acme.transport.network.node.nodescore

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalNodeScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalNodeScoreStore()
        val item = newRegionalNodeScore("code-173", "name-727")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
