package com.acme.transport.freightpay.accessorial.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingAccessorialGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingAccessorialGroupStore()
        val item = newPendingAccessorialGroup("code-752", "name-333")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
