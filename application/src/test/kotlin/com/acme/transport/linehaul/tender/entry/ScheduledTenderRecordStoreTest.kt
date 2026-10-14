package com.acme.transport.linehaul.tender.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledTenderRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledTenderRecordStore()
        val item = newScheduledTenderRecord("code-753", "title-343")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
