package com.acme.transport.terminal.dwell.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultDwellBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultDwellBudgetStore()
        val item = newDefaultDwellBudget("code-960", "title-373")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
