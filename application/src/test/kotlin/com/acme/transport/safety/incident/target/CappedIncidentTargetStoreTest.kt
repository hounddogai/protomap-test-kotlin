package com.acme.transport.safety.incident.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedIncidentTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedIncidentTargetStore()
        val item = newCappedIncidentTarget("code-349", "title-485")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
