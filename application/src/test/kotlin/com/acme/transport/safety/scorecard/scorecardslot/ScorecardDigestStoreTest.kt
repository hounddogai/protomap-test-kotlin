package com.acme.transport.safety.scorecard.scorecardslot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScorecardDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScorecardDigestStore()
        val item = newScorecardDigest("code-829", "label-947")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
