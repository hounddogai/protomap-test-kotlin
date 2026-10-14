package com.acme.transport.linehaul.trailer.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerDigestStore()
        val item = newTrailerDigest("code-205", "title-903")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
