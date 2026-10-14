package com.acme.transport.freightpay.audit.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingAuditMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingAuditMappingStore()
        val item = newRollingAuditMapping("code-734", "name-485")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
