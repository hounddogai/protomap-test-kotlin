package com.acme.transport.freightpay.accessorial.reservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientAccessorialAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientAccessorialAllocationStore()
        val item = newLenientAccessorialAllocation("code-572", "name-282")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
