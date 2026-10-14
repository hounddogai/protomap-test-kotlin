package com.acme.transport.lastmile.redelivery.ledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalRedeliveryLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalRedeliveryLedgerStore()
        val item = newGlobalRedeliveryLedger("code-953", "name-845")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
