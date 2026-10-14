package com.acme.transport.safety.cameraevent.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CameraEventLedgerStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCameraEventLedgerStore()
        val item = newCameraEventLedger("code-788", "title-813")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
