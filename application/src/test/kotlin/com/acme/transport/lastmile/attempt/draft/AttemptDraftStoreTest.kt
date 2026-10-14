package com.acme.transport.lastmile.attempt.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AttemptDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAttemptDraftStore()
        val item = newAttemptDraft("code-799", "title-569")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
