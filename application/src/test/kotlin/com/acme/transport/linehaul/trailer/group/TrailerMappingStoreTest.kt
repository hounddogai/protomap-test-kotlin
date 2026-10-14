package com.acme.transport.linehaul.trailer.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerMappingStore()
        val item = newTrailerMapping("code-298", "title-254")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
