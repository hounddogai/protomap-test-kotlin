package com.acme.transport.freightpay.audit.auditoverride

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalAuditBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalAuditBundleStore()
        val item = newGlobalAuditBundle("code-522", "title-969")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
