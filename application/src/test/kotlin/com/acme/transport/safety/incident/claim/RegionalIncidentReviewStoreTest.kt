package com.acme.transport.safety.incident.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalIncidentReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalIncidentReviewStore()
        val item = newRegionalIncidentReview("code-617", "name-618")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
