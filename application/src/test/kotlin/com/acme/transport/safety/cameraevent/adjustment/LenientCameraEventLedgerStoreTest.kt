package com.acme.transport.safety.cameraevent.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientCameraEventLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientCameraEventLedgerStore()
        val item = newLenientCameraEventLedger("code-758", "label-623")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
