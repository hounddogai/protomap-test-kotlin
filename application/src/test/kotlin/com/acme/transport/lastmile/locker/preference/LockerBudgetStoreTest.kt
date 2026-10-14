package com.acme.transport.lastmile.locker.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LockerBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLockerBudgetStore()
        val item = newLockerBudget("code-329", "name-92")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
