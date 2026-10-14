package com.acme.transport.freightpay.accessorial.accessorialcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AccessorialLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAccessorialLedgerStore()
        val item = newAccessorialLedger("code-622", "title-610")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
