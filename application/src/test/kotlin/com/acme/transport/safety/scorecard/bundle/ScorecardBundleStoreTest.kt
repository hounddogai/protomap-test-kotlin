package com.acme.transport.safety.scorecard.bundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScorecardBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScorecardBundleStore()
        val item = newScorecardBundle("code-288", "label-205")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
