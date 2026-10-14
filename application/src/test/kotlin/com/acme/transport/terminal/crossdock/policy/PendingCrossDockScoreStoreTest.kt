package com.acme.transport.terminal.crossdock.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingCrossDockScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingCrossDockScoreStore()
        val item = newPendingCrossDockScore("code-42", "title-107")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
