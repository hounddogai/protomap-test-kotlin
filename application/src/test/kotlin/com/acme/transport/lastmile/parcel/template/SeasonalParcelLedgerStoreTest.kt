package com.acme.transport.lastmile.parcel.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalParcelLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalParcelLedgerStore()
        val item = newSeasonalParcelLedger("code-840", "name-674")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
