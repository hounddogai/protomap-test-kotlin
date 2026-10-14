package com.acme.transport.terminal.dwell.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalDwellProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalDwellProposalStore()
        val item = newSeasonalDwellProposal("code-54", "title-58")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
