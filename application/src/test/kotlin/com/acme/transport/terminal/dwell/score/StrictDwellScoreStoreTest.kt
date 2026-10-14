package com.acme.transport.terminal.dwell.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictDwellScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictDwellScoreStore()
        val item = newStrictDwellScore("code-31", "name-900")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
