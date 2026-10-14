package com.acme.transport.crewing.swap.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SwapSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySwapSlotStore()
        val item = newSwapSlot("code-699", "title-413")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
