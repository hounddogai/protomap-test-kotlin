package com.acme.transport.crewing.swap.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SwapWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySwapWindowStore()
        val item = newSwapWindow("code-882", "label-567")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
