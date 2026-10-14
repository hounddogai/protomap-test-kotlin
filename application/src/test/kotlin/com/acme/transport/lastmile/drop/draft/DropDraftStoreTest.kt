package com.acme.transport.lastmile.drop.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DropDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDropDraftStore()
        val item = newDropDraft("code-107", "label-407")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
