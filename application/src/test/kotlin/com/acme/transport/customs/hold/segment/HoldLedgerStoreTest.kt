package com.acme.transport.customs.hold.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class HoldLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryHoldLedgerStore()
        val item = newHoldLedger("code-416", "label-86")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
