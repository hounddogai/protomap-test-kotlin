package com.acme.transport.network.zone.mapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ZoneReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryZoneReviewStore()
        val item = newZoneReview("code-188", "name-645")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
