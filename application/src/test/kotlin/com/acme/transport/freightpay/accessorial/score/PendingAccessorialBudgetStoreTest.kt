package com.acme.transport.freightpay.accessorial.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingAccessorialBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingAccessorialBudgetStore()
        val item = newPendingAccessorialBudget("code-516", "name-96")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
