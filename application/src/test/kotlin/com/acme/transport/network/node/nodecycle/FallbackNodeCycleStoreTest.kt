package com.acme.transport.network.node.nodecycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackNodeCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackNodeCycleStore()
        val item = newFallbackNodeCycle("code-994", "name-653")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
