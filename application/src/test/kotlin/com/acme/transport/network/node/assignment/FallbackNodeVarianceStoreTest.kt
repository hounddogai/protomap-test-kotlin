package com.acme.transport.network.node.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackNodeVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackNodeVarianceStore()
        val item = newFallbackNodeVariance("code-685", "label-189")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
