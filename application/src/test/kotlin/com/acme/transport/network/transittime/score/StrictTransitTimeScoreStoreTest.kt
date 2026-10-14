package com.acme.transport.network.transittime.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictTransitTimeScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictTransitTimeScoreStore()
        val item = newStrictTransitTimeScore("code-392", "name-252")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
