package com.acme.transport.emissions.fuelburn.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingFuelBurnProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingFuelBurnProposalStore()
        val item = newRollingFuelBurnProposal("code-10", "name-751")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
