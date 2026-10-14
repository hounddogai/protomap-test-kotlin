package com.acme.transport.crewing.swap.swaprecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackSwapGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackSwapGroupStore()
        val item = newFallbackSwapGroup("code-29", "label-535")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
