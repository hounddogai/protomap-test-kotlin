package com.acme.transport.linehaul.capacity.checkpoint

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CapacityDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCapacityDraftStore()
        val item = newCapacityDraft("code-200", "title-753")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
