package com.acme.transport.freightpay.accessorial.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledAccessorialBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledAccessorialBudgetStore()
        val item = newScheduledAccessorialBudget("code-7", "title-276")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
