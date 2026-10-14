package com.acme.transport.emissions.idling.idlingvariance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IdlingDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIdlingDraftStore()
        val item = newIdlingDraft("code-338", "label-568")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
