package com.acme.transport.crewing.restbreak.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalRestBreakLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalRestBreakLedgerStore()
        val item = newSeasonalRestBreakLedger("code-283", "title-227")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
