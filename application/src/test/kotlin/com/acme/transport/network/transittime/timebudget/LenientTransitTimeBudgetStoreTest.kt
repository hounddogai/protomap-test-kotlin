package com.acme.transport.network.transittime.timebudget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientTransitTimeBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientTransitTimeBudgetStore()
        val item = newLenientTransitTimeBudget("code-425", "title-186")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
