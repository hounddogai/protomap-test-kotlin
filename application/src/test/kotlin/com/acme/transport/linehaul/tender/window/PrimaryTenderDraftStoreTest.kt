package com.acme.transport.linehaul.tender.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryTenderDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryTenderDraftStore()
        val item = newPrimaryTenderDraft("code-688", "title-649")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
