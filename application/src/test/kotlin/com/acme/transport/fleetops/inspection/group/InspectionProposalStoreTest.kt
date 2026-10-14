package com.acme.transport.fleetops.inspection.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class InspectionProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryInspectionProposalStore()
        val item = newInspectionProposal("code-824", "title-459")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
