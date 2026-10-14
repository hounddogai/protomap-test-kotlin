package com.acme.transport.fleetops.tire.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TireProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTireProposalStore()
        val item = newTireProposal("code-268", "label-673")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
