package com.acme.transport.terminal.dockdoor.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalDockDoorTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalDockDoorTemplateStore()
        val item = newGlobalDockDoorTemplate("code-790", "name-360")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
