package com.acme.transport.network.node.nodedraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingNodeDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingNodeDraftStore()
        val item = newRollingNodeDraft("code-445", "title-608")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
