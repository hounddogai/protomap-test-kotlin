package com.acme.transport.crewing.swap.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SwapEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySwapEntryStore()
        val item = newSwapEntry("code-713", "name-210")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
