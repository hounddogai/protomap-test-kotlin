package com.acme.transport.freightpay.audit.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedAuditMappingStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedAuditMappingStore()
        val item = newWeightedAuditMapping("code-240", "title-970")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
