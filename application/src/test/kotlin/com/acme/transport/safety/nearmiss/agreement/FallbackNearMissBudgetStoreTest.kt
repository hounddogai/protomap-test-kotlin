package com.acme.transport.safety.nearmiss.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackNearMissBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackNearMissBudgetStore()
        val item = newFallbackNearMissBudget("code-54", "label-626")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
