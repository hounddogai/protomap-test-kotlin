package com.acme.transport.lastmile.parcel.parcelbaseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredParcelBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredParcelBaselineStore()
        val item = newTieredParcelBaseline("code-104", "title-154")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
