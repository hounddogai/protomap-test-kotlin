package com.acme.transport.crewing.swap.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SwapAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySwapAdjustmentStore()
        val item = newSwapAdjustment("code-308", "label-845")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
