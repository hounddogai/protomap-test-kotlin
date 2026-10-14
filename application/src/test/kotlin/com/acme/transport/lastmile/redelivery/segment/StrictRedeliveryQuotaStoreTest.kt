package com.acme.transport.lastmile.redelivery.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictRedeliveryQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictRedeliveryQuotaStore()
        val item = newStrictRedeliveryQuota("code-562", "title-18")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
