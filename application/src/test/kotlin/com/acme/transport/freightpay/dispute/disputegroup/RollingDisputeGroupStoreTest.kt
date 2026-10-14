package com.acme.transport.freightpay.dispute.disputegroup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingDisputeGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingDisputeGroupStore()
        val item = newRollingDisputeGroup("code-929", "title-430")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
