package com.acme.transport.crewing.swap.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedSwapSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedSwapSlotStore()
        val item = newWeightedSwapSlot("code-197", "name-482")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
