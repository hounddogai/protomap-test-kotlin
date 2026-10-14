package com.acme.transport.lastmile.attempt.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientAttemptLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientAttemptLedgerStore()
        val item = newLenientAttemptLedger("code-838", "title-216")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
