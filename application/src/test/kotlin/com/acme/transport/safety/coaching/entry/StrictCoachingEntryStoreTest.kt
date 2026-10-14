package com.acme.transport.safety.coaching.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictCoachingEntryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictCoachingEntryStore()
        val item = newStrictCoachingEntry("code-67", "name-480")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
