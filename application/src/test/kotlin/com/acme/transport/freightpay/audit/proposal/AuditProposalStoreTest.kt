package com.acme.transport.freightpay.audit.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AuditProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAuditProposalStore()
        val item = newAuditProposal("code-958", "name-83")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
