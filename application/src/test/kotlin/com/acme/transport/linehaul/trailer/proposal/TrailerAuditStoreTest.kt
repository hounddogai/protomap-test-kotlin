package com.acme.transport.linehaul.trailer.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerAuditStore()
        val item = newTrailerAudit("code-926", "name-764")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
