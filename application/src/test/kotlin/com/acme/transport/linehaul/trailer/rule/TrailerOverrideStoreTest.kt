package com.acme.transport.linehaul.trailer.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerOverrideStore()
        val item = newTrailerOverride("code-205", "title-277")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
