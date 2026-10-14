package com.acme.transport.linehaul.backhaul.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BackhaulDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBackhaulDraftStore()
        val item = newBackhaulDraft("code-931", "label-913")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
