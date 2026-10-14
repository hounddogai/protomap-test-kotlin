package com.acme.transport.terminal.dwell.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedDwellDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedDwellDraftStore()
        val item = newCappedDwellDraft("code-734", "name-58")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
