package com.acme.transport.network.node.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedNodeWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedNodeWindowStore()
        val item = newWeightedNodeWindow("code-847", "name-508")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
