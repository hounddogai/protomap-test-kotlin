package com.acme.transport.crewing.swap.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveSwapBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveSwapBudgetStore()
        val item = newAdaptiveSwapBudget("code-167", "title-858")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
