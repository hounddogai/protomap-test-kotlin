package com.acme.transport.safety.coaching.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingCoachingLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingCoachingLedgerStore()
        val item = newPendingCoachingLedger("code-292", "title-430")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
