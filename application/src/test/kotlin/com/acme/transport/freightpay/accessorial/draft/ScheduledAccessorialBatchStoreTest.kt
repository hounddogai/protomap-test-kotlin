package com.acme.transport.freightpay.accessorial.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledAccessorialBatchStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledAccessorialBatchStore()
        val item = newScheduledAccessorialBatch("code-278", "name-294")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
