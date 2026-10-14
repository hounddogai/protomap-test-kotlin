package com.acme.transport.network.cutoff.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultCutoffAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultCutoffAuditStore()
        val item = newDefaultCutoffAudit("code-423", "name-436")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
