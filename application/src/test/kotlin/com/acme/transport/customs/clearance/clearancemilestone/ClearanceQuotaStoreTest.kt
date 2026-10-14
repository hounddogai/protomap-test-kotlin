package com.acme.transport.customs.clearance.clearancemilestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ClearanceQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryClearanceQuotaStore()
        val item = newClearanceQuota("code-126", "title-320")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
