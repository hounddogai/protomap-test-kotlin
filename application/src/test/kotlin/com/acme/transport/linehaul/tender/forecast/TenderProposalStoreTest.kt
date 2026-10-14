package com.acme.transport.linehaul.tender.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TenderProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTenderProposalStore()
        val item = newTenderProposal("code-788", "label-613")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
