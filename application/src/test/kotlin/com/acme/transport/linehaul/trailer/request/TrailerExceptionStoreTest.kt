package com.acme.transport.linehaul.trailer.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerExceptionStore()
        val item = newTrailerException("code-818", "label-817")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
