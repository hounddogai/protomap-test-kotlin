package com.acme.transport.terminal.dockdoor.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DockDoorBudgetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDockDoorBudgetStore()
        val item = newDockDoorBudget("code-530", "title-787")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
