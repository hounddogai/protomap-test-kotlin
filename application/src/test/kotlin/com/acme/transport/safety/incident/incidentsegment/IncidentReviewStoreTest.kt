package com.acme.transport.safety.incident.incidentsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IncidentReviewStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIncidentReviewStore()
        val item = newIncidentReview("code-977", "name-392")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
