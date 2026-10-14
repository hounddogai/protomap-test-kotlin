package com.acme.transport.network.cutoff.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingCutoffDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingCutoffDraftStore()
        val item = newRollingCutoffDraft("code-420", "name-964")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
