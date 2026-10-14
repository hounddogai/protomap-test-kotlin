package com.acme.transport.customs.clearance.clearancecheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ClearanceProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryClearanceProposalStore()
        val item = newClearanceProposal("code-469", "title-636")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
