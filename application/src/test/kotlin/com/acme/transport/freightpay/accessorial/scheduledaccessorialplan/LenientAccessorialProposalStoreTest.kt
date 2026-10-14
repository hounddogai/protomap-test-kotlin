package com.acme.transport.freightpay.accessorial.scheduledaccessorialplan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientAccessorialProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientAccessorialProposalStore()
        val item = newLenientAccessorialProposal("code-374", "name-766")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
