package com.acme.transport.terminal.crossdock.strictdockslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedCrossDockSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedCrossDockSlotStore()
        val item = newWeightedCrossDockSlot("code-854", "name-938")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
