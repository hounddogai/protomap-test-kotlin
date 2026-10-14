package com.acme.transport.crewing.swap.swapschedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SwapCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySwapCycleStore()
        val item = newSwapCycle("code-56", "label-914")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
