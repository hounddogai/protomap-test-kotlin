package com.acme.transport.freightpay.dispute.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultDisputeTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultDisputeTemplateStore()
        val item = newDefaultDisputeTemplate("code-316", "title-336")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
