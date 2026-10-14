package com.acme.transport.network.hub.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedHubDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedHubDraftStore()
        val item = newWeightedHubDraft("code-879", "label-237")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
