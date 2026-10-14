package com.acme.transport.freightpay.dispute.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DisputeWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDisputeWindowStore()
        val item = newDisputeWindow("code-168", "title-747")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
