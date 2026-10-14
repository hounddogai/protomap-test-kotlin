package com.acme.transport.crewing.swap.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SwapLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySwapLedgerStore()
        val item = newSwapLedger("code-905", "title-217")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
