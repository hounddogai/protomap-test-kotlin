package com.acme.transport.lastmile.redelivery.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalRedeliveryLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalRedeliveryLedgerStore()
        val item = newSeasonalRedeliveryLedger("code-580", "label-920")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
