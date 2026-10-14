package com.acme.transport.freightpay.audit.lenientauditproposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalAuditProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalAuditProposalStore()
        val item = newGlobalAuditProposal("code-434", "title-543")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
