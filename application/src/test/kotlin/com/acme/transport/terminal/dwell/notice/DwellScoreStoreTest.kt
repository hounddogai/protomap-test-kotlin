package com.acme.transport.terminal.dwell.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DwellScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDwellScoreStore()
        val item = newDwellScore("code-787", "label-258")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
