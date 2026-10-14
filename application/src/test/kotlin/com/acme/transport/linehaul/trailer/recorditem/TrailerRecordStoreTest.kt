package com.acme.transport.linehaul.trailer.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerRecordStore()
        val item = newTrailerRecord("code-143", "label-948")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
