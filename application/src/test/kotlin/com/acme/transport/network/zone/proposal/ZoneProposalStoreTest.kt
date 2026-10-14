package com.acme.transport.network.zone.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ZoneProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryZoneProposalStore()
        val item = newZoneProposal("code-665", "title-915")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
