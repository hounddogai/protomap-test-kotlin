package com.acme.transport.linehaul.tender.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryTenderQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryTenderQuotaStore()
        val item = newPrimaryTenderQuota("code-674", "title-807")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
