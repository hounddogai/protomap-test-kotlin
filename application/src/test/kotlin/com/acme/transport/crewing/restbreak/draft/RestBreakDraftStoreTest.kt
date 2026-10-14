package com.acme.transport.crewing.restbreak.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RestBreakDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRestBreakDraftStore()
        val item = newRestBreakDraft("code-778", "name-364")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
