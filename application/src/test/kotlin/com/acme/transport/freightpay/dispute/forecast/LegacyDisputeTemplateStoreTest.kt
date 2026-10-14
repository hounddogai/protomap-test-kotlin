package com.acme.transport.freightpay.dispute.forecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyDisputeTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyDisputeTemplateStore()
        val item = newLegacyDisputeTemplate("code-553", "label-755")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
