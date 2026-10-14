package com.acme.transport.safety.violation.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingViolationRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingViolationRollupStore()
        val item = newPendingViolationRollup("code-422", "name-880")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
