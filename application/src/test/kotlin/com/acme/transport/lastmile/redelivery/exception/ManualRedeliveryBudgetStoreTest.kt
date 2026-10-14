package com.acme.transport.lastmile.redelivery.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualRedeliveryBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualRedeliveryBudgetStore()
        val item = newManualRedeliveryBudget("code-293", "label-490")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
