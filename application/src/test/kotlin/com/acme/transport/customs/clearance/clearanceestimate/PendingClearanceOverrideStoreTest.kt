package com.acme.transport.customs.clearance.clearanceestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingClearanceOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingClearanceOverrideStore()
        val item = newPendingClearanceOverride("code-471", "label-345")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
