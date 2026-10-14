package com.acme.transport.freightpay.accessorial.accessorialbudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualAccessorialBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualAccessorialBudgetStore()
        val item = newManualAccessorialBudget("code-727", "label-195")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
