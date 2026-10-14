package com.acme.transport.lastmile.attempt.attemptdraft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalAttemptEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalAttemptEntryStore()
        val item = newSeasonalAttemptEntry("code-761", "title-91")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
