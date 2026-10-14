package com.acme.transport.safety.nearmiss.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultNearMissAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultNearMissAuditStore()
        val item = newDefaultNearMissAudit("code-850", "title-810")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
