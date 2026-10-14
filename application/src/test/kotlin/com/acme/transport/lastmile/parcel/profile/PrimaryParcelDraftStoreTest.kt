package com.acme.transport.lastmile.parcel.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryParcelDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryParcelDraftStore()
        val item = newPrimaryParcelDraft("code-594", "label-332")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
