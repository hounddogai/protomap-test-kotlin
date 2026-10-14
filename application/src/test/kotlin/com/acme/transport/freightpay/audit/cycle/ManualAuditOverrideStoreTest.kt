package com.acme.transport.freightpay.audit.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualAuditOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualAuditOverrideStore()
        val item = newManualAuditOverride("code-647", "name-137")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
