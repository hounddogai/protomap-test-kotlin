package com.acme.transport.emissions.idling.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingIdlingProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingIdlingProposalStore()
        val item = newRollingIdlingProposal("code-881", "name-250")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
