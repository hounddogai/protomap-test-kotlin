package com.acme.transport.terminal.traileryard.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredTrailerYardBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredTrailerYardBudgetStore()
        val item = newTieredTrailerYardBudget("code-599", "label-784")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
