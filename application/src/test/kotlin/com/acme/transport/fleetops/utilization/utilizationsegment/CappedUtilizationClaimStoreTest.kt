package com.acme.transport.fleetops.utilization.utilizationsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedUtilizationClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedUtilizationClaimStore()
        val item = newCappedUtilizationClaim("code-885", "title-779")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
