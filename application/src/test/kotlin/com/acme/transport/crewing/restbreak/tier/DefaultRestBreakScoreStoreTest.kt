package com.acme.transport.crewing.restbreak.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultRestBreakScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultRestBreakScoreStore()
        val item = newDefaultRestBreakScore("code-195", "label-928")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
