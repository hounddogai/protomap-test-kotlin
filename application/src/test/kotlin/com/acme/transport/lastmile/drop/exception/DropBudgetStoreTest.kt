package com.acme.transport.lastmile.drop.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DropBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDropBudgetStore()
        val item = newDropBudget("code-177", "name-412")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
