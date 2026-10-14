package com.acme.transport.safety.nearmiss.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingNearMissBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingNearMissBudgetStore()
        val item = newPendingNearMissBudget("code-940", "title-323")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
