package com.acme.transport.fleetops.leasereturn.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveLeaseReturnDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveLeaseReturnDraftStore()
        val item = newAdaptiveLeaseReturnDraft("code-747", "name-98")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
