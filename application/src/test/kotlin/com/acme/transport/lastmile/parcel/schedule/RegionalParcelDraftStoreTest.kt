package com.acme.transport.lastmile.parcel.schedule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalParcelDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalParcelDraftStore()
        val item = newRegionalParcelDraft("code-96", "label-415")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
