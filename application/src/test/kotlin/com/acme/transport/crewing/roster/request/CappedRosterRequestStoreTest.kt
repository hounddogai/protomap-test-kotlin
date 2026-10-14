package com.acme.transport.crewing.roster.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedRosterRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedRosterRequestStore()
        val item = newCappedRosterRequest("code-79", "name-238")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
