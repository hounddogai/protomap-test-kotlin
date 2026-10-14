package com.acme.transport.linehaul.trailer.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalTrailerScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalTrailerScoreStore()
        val item = newGlobalTrailerScore("code-951", "name-580")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
