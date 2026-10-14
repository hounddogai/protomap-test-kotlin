package com.acme.transport.linehaul.tender.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TenderDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTenderDraftStore()
        val item = newTenderDraft("code-859", "label-349")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
