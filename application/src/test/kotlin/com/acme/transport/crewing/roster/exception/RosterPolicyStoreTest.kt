package com.acme.transport.crewing.roster.exception

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RosterPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRosterPolicyStore()
        val item = newRosterPolicy("code-880", "label-28")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
