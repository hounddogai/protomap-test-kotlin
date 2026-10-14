package com.acme.transport.freightpay.dispute.manifest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingDisputeAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingDisputeAdjustmentStore()
        val item = newRollingDisputeAdjustment("code-313", "name-891")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
