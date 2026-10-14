package com.acme.transport.linehaul.tender.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveTenderProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveTenderProposalStore()
        val item = newAdaptiveTenderProposal("code-847", "title-163")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
