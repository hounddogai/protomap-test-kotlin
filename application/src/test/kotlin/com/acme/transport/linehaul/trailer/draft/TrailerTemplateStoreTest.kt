package com.acme.transport.linehaul.trailer.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerTemplateStore()
        val item = newTrailerTemplate("code-110", "title-971")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
