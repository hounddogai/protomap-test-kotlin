package com.acme.transport.fleetops.leasereturn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveLeaseReturnDigestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveLeaseReturnDigestStore()
        val item = newAdaptiveLeaseReturnDigest("code-994", "title-853")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
