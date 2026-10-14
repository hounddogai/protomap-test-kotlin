package com.acme.transport.fleetops.tire.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingTireBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingTireBudgetStore()
        val item = newRollingTireBudget("code-737", "title-635")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
