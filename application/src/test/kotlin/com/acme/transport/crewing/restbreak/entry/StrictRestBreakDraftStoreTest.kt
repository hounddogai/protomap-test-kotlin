package com.acme.transport.crewing.restbreak.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictRestBreakDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictRestBreakDraftStore()
        val item = newStrictRestBreakDraft("code-287", "name-53")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
