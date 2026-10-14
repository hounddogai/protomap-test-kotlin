package com.acme.transport.emissions.fuelburn.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledFuelBurnProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledFuelBurnProposalStore()
        val item = newScheduledFuelBurnProposal("code-980", "name-615")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
