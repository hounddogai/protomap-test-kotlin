package com.acme.transport.terminal.dwell.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackDwellScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackDwellScoreStore()
        val item = newFallbackDwellScore("code-650", "name-640")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
