package com.acme.transport.freightpay.audit.legacyauditpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveAuditTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveAuditTemplateStore()
        val item = newAdaptiveAuditTemplate("code-1", "title-974")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
