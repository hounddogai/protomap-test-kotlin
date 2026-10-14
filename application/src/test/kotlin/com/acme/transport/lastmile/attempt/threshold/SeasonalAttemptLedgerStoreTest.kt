package com.acme.transport.lastmile.attempt.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalAttemptLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalAttemptLedgerStore()
        val item = newSeasonalAttemptLedger("code-297", "title-111")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
