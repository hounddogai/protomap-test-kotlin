package com.acme.transport.linehaul.tender.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryTenderExceptionStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryTenderExceptionStore()
        val item = newPrimaryTenderException("code-914", "title-853")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
