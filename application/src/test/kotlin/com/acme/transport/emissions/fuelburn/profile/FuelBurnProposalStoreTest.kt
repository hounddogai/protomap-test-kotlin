package com.acme.transport.emissions.fuelburn.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FuelBurnProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFuelBurnProposalStore()
        val item = newFuelBurnProposal("code-761", "name-265")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
