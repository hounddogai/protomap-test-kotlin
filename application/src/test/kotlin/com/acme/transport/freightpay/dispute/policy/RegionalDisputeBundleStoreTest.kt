package com.acme.transport.freightpay.dispute.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalDisputeBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalDisputeBundleStore()
        val item = newRegionalDisputeBundle("code-987", "name-162")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
