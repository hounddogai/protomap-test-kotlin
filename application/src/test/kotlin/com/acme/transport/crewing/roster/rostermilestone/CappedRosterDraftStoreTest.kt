package com.acme.transport.crewing.roster.rostermilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedRosterDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedRosterDraftStore()
        val item = newCappedRosterDraft("code-7", "label-171")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
