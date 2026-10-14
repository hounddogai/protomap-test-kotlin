package com.acme.transport.customs.hold.strictholdaudit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictHoldAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictHoldAuditStore()
        val item = newStrictHoldAudit("code-249", "label-640")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
