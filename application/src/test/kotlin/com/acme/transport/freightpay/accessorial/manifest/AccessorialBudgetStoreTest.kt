package com.acme.transport.freightpay.accessorial.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AccessorialBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAccessorialBudgetStore()
        val item = newAccessorialBudget("code-809", "title-633")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
