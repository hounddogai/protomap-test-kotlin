package com.acme.transport.crewing.roster.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackRosterRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackRosterRequestStore()
        val item = newFallbackRosterRequest("code-811", "name-212")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
