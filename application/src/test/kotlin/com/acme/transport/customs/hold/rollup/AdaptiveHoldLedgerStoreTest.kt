package com.acme.transport.customs.hold.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveHoldLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveHoldLedgerStore()
        val item = newAdaptiveHoldLedger("code-843", "label-150")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
