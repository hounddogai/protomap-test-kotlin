package com.acme.transport.terminal.dockdoor.tiereddooragreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingDockDoorTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingDockDoorTemplateStore()
        val item = newRollingDockDoorTemplate("code-304", "title-219")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
