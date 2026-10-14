package com.acme.transport.network.cutoff.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledCutoffDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledCutoffDraftStore()
        val item = newScheduledCutoffDraft("code-670", "label-29")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
