package com.acme.transport.freightpay.accessorial.cycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AccessorialBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAccessorialBatchStore()
        val item = newAccessorialBatch("code-375", "label-42")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
