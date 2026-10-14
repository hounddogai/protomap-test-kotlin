package com.acme.transport.lastmile.parcel.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalParcelEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalParcelEntryStore()
        val item = newRegionalParcelEntry("code-901", "label-49")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
