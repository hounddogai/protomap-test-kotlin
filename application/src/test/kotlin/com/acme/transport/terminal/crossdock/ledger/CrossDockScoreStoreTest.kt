package com.acme.transport.terminal.crossdock.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CrossDockScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCrossDockScoreStore()
        val item = newCrossDockScore("code-871", "label-449")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
