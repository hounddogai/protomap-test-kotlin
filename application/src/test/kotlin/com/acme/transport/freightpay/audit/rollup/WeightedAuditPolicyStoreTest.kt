package com.acme.transport.freightpay.audit.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedAuditPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedAuditPolicyStore()
        val item = newWeightedAuditPolicy("code-106", "name-642")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
