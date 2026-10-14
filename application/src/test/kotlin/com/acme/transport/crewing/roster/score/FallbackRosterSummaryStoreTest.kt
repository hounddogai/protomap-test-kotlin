package com.acme.transport.crewing.roster.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackRosterSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackRosterSummaryStore()
        val item = newFallbackRosterSummary("code-349", "label-801")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
