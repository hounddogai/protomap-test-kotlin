package com.acme.transport.freightpay.accessorial.accessorialmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class BatchedAccessorialReservationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryBatchedAccessorialReservationStore()
        val item = newBatchedAccessorialReservation("code-480", "title-243")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
