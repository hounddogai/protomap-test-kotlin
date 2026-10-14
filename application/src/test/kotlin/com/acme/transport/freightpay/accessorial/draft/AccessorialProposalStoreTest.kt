package com.acme.transport.freightpay.accessorial.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AccessorialProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAccessorialProposalStore()
        val item = newAccessorialProposal("code-171", "label-843")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
