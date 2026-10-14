package com.acme.transport.linehaul.tender.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalTenderQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalTenderQuotaStore()
        val item = newGlobalTenderQuota("code-120", "label-665")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
