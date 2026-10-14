package com.acme.transport.fleetops.utilization.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class UtilizationProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryUtilizationProposalStore()
        val item = newUtilizationProposal("code-897", "name-843")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
