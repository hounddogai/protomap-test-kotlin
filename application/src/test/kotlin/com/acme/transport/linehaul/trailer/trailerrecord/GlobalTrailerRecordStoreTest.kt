package com.acme.transport.linehaul.trailer.trailerrecord

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalTrailerRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalTrailerRecordStore()
        val item = newGlobalTrailerRecord("code-461", "name-663")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
