package com.acme.transport.freightpay.audit.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalAuditWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalAuditWindowStore()
        val item = newRegionalAuditWindow("code-685", "name-684")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
