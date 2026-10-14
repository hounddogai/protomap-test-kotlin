package com.acme.transport.freightpay.audit.auditpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictAuditProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictAuditProposalStore()
        val item = newStrictAuditProposal("code-884", "title-110")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
