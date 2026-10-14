package com.acme.transport.lastmile.proofofdelivery.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalProofOfDeliveryAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalProofOfDeliveryAllocationStore()
        val item = newGlobalProofOfDeliveryAllocation("code-985", "name-775")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
