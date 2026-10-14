package com.acme.transport.crewing.swap.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientSwapProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientSwapProposalStore()
        val item = newLenientSwapProposal("code-348", "name-277")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
