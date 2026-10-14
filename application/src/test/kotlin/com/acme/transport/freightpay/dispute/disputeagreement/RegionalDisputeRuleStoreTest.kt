package com.acme.transport.freightpay.dispute.disputeagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalDisputeRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalDisputeRuleStore()
        val item = newRegionalDisputeRule("code-25", "label-439")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
