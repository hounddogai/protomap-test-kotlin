package com.acme.transport.freightpay.audit.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackAuditTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackAuditTargetStore()
        val item = newFallbackAuditTarget("code-282", "name-751")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
