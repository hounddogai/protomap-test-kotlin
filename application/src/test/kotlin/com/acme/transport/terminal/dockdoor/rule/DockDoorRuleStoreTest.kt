package com.acme.transport.terminal.dockdoor.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DockDoorRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDockDoorRuleStore()
        val item = newDockDoorRule("code-377", "name-185")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
