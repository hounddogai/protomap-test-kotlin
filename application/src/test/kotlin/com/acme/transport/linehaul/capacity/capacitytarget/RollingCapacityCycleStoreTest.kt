package com.acme.transport.linehaul.capacity.capacitytarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingCapacityCycleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingCapacityCycleStore()
        val item = newRollingCapacityCycle("code-331", "label-752")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
