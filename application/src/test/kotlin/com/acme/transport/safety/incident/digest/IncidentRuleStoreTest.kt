package com.acme.transport.safety.incident.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class IncidentRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryIncidentRuleStore()
        val item = newIncidentRule("code-805", "label-513")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
