package com.acme.transport.safety.scorecard.scorecardwindow

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientScorecardWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientScorecardWindowStore()
        val item = newLenientScorecardWindow("code-81", "title-319")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
