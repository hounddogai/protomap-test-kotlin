package com.acme.transport.freightpay.dispute.disputebundle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalDisputeBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalDisputeBatchStore()
        val item = newGlobalDisputeBatch("code-71", "title-155")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
