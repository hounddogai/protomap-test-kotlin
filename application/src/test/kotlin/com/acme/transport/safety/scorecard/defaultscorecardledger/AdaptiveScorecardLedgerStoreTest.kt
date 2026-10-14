package com.acme.transport.safety.scorecard.defaultscorecardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveScorecardLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveScorecardLedgerStore()
        val item = newAdaptiveScorecardLedger("code-852", "name-219")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
