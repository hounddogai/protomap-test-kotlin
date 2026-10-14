package com.acme.transport.safety.nearmiss.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class NearMissBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryNearMissBudgetStore()
        val item = newNearMissBudget("code-35", "name-10")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
