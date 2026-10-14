package com.acme.transport.linehaul.tender.plan

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TenderVarianceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTenderVarianceStore()
        val item = newTenderVariance("code-800", "title-429")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
