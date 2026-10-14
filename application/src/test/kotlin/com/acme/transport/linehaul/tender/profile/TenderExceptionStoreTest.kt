package com.acme.transport.linehaul.tender.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TenderExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTenderExceptionStore()
        val item = newTenderException("code-800", "name-948")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
