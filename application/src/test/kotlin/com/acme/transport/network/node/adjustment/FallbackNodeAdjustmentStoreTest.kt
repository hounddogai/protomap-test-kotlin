package com.acme.transport.network.node.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackNodeAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackNodeAdjustmentStore()
        val item = newFallbackNodeAdjustment("code-970", "label-455")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
