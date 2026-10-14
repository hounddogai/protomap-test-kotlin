package com.acme.transport.terminal.traileryard.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyTrailerYardClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyTrailerYardClaimStore()
        val item = newLegacyTrailerYardClaim("code-961", "name-257")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
