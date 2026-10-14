package com.acme.transport.linehaul.capacity.capacitytarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalCapacityTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalCapacityTargetStore()
        val item = newRegionalCapacityTarget("code-421", "name-721")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
