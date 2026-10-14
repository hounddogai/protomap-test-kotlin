package com.acme.transport.terminal.crossdock.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalCrossDockAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalCrossDockAuditStore()
        val item = newGlobalCrossDockAudit("code-189", "label-863")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
