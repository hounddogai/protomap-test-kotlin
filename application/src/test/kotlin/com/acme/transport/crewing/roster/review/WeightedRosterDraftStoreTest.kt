package com.acme.transport.crewing.roster.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedRosterDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedRosterDraftStore()
        val item = newWeightedRosterDraft("code-157", "label-400")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
