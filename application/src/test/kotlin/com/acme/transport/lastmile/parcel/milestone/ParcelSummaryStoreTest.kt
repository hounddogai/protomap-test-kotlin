package com.acme.transport.lastmile.parcel.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ParcelSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryParcelSummaryStore()
        val item = newParcelSummary("code-150", "title-219")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
