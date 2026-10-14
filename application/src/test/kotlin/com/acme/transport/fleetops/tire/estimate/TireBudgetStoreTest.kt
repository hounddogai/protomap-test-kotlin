package com.acme.transport.fleetops.tire.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TireBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTireBudgetStore()
        val item = newTireBudget("code-593", "title-558")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
