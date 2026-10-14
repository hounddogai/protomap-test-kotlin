package com.acme.transport.linehaul.tender.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyTenderRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyTenderRecordStore()
        val item = newLegacyTenderRecord("code-528", "title-773")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
