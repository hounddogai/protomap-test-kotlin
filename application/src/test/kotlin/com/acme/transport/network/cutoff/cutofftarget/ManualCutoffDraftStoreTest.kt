package com.acme.transport.network.cutoff.cutofftarget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualCutoffDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualCutoffDraftStore()
        val item = newManualCutoffDraft("code-389", "label-128")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
