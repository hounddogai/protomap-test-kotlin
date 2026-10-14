package com.acme.transport.linehaul.relay.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientRelayScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientRelayScoreStore()
        val item = newLenientRelayScore("code-283", "label-751")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
