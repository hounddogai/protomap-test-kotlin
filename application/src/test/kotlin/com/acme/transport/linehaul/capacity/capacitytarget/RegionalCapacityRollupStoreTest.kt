package com.acme.transport.linehaul.capacity.capacitytarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalCapacityRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalCapacityRollupStore()
        val item = newRegionalCapacityRollup("code-101", "label-341")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
