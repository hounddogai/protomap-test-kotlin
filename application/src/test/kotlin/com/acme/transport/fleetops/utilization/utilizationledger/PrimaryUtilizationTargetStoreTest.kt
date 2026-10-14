package com.acme.transport.fleetops.utilization.utilizationledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryUtilizationTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryUtilizationTargetStore()
        val item = newPrimaryUtilizationTarget("code-280", "label-483")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
