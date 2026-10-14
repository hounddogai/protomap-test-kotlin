package com.acme.transport.network.zone.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalZoneReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalZoneReviewStore()
        val item = newSeasonalZoneReview("code-20", "name-183")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
