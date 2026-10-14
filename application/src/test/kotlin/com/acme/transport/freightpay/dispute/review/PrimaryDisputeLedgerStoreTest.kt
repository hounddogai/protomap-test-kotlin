package com.acme.transport.freightpay.dispute.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryDisputeLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryDisputeLedgerStore()
        val item = newPrimaryDisputeLedger("code-722", "label-11")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
