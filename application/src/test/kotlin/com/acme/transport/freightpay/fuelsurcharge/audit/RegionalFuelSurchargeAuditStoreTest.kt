package com.acme.transport.freightpay.fuelsurcharge.audit

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalFuelSurchargeAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalFuelSurchargeAuditStore()
        val item = newRegionalFuelSurchargeAudit("code-17", "name-290")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
