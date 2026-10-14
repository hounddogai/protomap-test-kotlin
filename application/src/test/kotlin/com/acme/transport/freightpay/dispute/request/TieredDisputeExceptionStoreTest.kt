package com.acme.transport.freightpay.dispute.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredDisputeExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredDisputeExceptionStore()
        val item = newTieredDisputeException("code-568", "name-613")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
