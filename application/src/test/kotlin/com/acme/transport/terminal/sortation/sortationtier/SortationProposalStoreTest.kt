package com.acme.transport.terminal.sortation.sortationtier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SortationProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySortationProposalStore()
        val item = newSortationProposal("code-221", "title-779")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
