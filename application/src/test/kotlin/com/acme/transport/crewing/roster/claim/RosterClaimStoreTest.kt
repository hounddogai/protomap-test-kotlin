package com.acme.transport.crewing.roster.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RosterClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRosterClaimStore()
        val item = newRosterClaim("code-494", "name-31")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
