package com.acme.transport.terminal.dockdoor.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalDockDoorProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalDockDoorProposalStore()
        val item = newRegionalDockDoorProposal("code-430", "name-8")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
