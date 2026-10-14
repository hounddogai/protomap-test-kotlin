package com.acme.transport.customs.hold.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalHoldSlotStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalHoldSlotStore()
        val item = newRegionalHoldSlot("code-370", "name-427")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
