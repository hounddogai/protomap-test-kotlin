package com.acme.transport.terminal.dockdoor.doorclaim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalDockDoorClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalDockDoorClaimStore()
        val item = newRegionalDockDoorClaim("code-756", "name-817")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
