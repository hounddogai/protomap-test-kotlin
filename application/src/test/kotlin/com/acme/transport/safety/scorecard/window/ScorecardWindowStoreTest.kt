package com.acme.transport.safety.scorecard.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScorecardWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScorecardWindowStore()
        val item = newScorecardWindow("code-549", "title-280")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
