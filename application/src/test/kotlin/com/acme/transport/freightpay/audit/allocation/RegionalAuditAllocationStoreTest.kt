package com.acme.transport.freightpay.audit.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalAuditAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalAuditAllocationStore()
        val item = newRegionalAuditAllocation("code-620", "title-272")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
