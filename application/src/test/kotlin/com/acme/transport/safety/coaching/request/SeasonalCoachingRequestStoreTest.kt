package com.acme.transport.safety.coaching.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalCoachingRequestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalCoachingRequestStore()
        val item = newSeasonalCoachingRequest("code-109", "title-724")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
