package com.acme.transport.linehaul.tender.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedTenderProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedTenderProposalStore()
        val item = newBatchedTenderProposal("code-968", "title-739")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
