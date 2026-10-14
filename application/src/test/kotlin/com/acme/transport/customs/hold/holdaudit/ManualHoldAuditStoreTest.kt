package com.acme.transport.customs.hold.holdaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualHoldAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualHoldAuditStore()
        val item = newManualHoldAudit("code-955", "name-884")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
