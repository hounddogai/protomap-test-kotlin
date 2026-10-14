package com.acme.transport.terminal.dockdoor.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedDockDoorTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedDockDoorTemplateStore()
        val item = newCappedDockDoorTemplate("code-499", "label-16")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
