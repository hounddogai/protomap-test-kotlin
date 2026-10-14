package com.acme.transport.crewing.availability.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AvailabilityScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAvailabilityScoreStore()
        val item = newAvailabilityScore("code-695", "title-883")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
