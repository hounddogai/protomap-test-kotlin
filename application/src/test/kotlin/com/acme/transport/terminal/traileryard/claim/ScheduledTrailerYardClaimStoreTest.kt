package com.acme.transport.terminal.traileryard.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledTrailerYardClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledTrailerYardClaimStore()
        val item = newScheduledTrailerYardClaim("code-762", "name-285")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
