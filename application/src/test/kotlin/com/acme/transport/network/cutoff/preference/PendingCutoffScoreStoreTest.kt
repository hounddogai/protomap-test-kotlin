package com.acme.transport.network.cutoff.preference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingCutoffScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingCutoffScoreStore()
        val item = newPendingCutoffScore("code-969", "label-691")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
