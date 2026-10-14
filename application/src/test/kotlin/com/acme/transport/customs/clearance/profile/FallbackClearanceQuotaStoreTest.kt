package com.acme.transport.customs.clearance.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackClearanceQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackClearanceQuotaStore()
        val item = newFallbackClearanceQuota("code-520", "label-369")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
