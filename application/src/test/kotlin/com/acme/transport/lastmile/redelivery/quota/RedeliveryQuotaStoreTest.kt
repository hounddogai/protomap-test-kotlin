package com.acme.transport.lastmile.redelivery.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RedeliveryQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRedeliveryQuotaStore()
        val item = newRedeliveryQuota("code-580", "label-228")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
