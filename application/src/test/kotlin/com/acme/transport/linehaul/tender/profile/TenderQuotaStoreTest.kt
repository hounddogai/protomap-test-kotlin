package com.acme.transport.linehaul.tender.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TenderQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTenderQuotaStore()
        val item = newTenderQuota("code-277", "label-439")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
