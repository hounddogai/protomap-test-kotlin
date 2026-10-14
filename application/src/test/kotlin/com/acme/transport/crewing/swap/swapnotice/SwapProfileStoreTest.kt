package com.acme.transport.crewing.swap.swapnotice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SwapProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySwapProfileStore()
        val item = newSwapProfile("code-859", "name-526")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
