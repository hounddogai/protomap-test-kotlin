package com.acme.transport.terminal.crossdock.variance

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CrossDockAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCrossDockAuditStore()
        val item = newCrossDockAudit("code-262", "name-689")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
