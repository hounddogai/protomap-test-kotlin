package com.acme.transport.safety.incident.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IncidentTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIncidentTierStore()
        val item = newIncidentTier("code-285", "label-178")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
