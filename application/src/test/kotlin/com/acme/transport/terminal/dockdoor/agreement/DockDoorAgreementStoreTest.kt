package com.acme.transport.terminal.dockdoor.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DockDoorAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDockDoorAgreementStore()
        val item = newDockDoorAgreement("code-398", "name-983")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
