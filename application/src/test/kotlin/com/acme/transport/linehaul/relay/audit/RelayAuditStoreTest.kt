package com.acme.transport.linehaul.relay.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RelayAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRelayAuditStore()
        val item = newRelayAudit("code-855", "name-553")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
