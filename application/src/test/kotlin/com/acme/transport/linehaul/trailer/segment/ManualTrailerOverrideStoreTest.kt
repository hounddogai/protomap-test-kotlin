package com.acme.transport.linehaul.trailer.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualTrailerOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualTrailerOverrideStore()
        val item = newManualTrailerOverride("code-698", "title-992")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
