package com.acme.transport.freightpay.dispute.window

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DisputeTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDisputeTemplateStore()
        val item = newDisputeTemplate("code-527", "name-712")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
