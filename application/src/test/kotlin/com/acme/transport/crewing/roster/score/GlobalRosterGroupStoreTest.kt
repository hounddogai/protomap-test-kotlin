package com.acme.transport.crewing.roster.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalRosterGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalRosterGroupStore()
        val item = newGlobalRosterGroup("code-285", "title-577")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
