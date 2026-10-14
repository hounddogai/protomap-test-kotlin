package com.acme.transport.network.hub.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HubDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHubDraftStore()
        val item = newHubDraft("code-222", "name-120")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
