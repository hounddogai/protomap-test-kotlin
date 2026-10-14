package com.acme.transport.linehaul.capacity.capacitymapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingCapacityRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingCapacityRollupStore()
        val item = newRollingCapacityRollup("code-839", "title-436")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
