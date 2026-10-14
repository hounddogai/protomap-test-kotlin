package com.acme.transport.customs.clearance.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyClearanceGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyClearanceGroupStore()
        val item = newLegacyClearanceGroup("code-988", "label-891")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
