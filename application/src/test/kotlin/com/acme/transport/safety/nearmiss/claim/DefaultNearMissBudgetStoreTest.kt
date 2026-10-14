package com.acme.transport.safety.nearmiss.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultNearMissBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultNearMissBudgetStore()
        val item = newDefaultNearMissBudget("code-63", "name-195")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
