package com.acme.transport.linehaul.backhaul.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BackhaulAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBackhaulAuditStore()
        val item = newBackhaulAudit("code-277", "label-445")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
