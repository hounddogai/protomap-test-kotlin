package com.acme.transport.emissions.evcharge.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class EvChargeAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryEvChargeAuditStore()
        val item = newEvChargeAudit("code-68", "label-46")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
