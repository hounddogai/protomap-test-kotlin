package com.acme.transport.safety.cameraevent.eventledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedCameraEventLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedCameraEventLedgerStore()
        val item = newCappedCameraEventLedger("code-511", "name-584")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
