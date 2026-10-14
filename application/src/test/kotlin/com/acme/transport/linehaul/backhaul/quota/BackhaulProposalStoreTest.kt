package com.acme.transport.linehaul.backhaul.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BackhaulProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBackhaulProposalStore()
        val item = newBackhaulProposal("code-3", "name-652")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
