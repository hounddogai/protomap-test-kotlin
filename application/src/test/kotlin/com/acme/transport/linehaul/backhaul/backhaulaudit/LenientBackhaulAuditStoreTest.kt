package com.acme.transport.linehaul.backhaul.backhaulaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientBackhaulAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientBackhaulAuditStore()
        val item = newLenientBackhaulAudit("code-151", "name-45")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
