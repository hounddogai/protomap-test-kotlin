package com.acme.transport.safety.scorecard.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScorecardManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScorecardManifestStore()
        val item = newScorecardManifest("code-777", "title-618")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
