package com.acme.transport.crewing.swap.slot

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingSwapLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingSwapLedgerStore()
        val item = newRollingSwapLedger("code-938", "label-468")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
