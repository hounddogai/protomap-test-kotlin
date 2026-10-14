package com.acme.transport.freightpay.dispute.disputebundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledDisputeBundleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledDisputeBundleStore()
        val item = newScheduledDisputeBundle("code-598", "label-578")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
