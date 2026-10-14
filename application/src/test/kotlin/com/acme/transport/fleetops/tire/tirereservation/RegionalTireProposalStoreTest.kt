package com.acme.transport.fleetops.tire.tirereservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalTireProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalTireProposalStore()
        val item = newRegionalTireProposal("code-189", "name-519")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
