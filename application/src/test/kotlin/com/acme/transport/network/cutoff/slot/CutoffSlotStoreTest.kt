package com.acme.transport.network.cutoff.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CutoffSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCutoffSlotStore()
        val item = newCutoffSlot("code-538", "title-149")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
