package com.acme.transport.terminal.traileryard.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictTrailerYardAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictTrailerYardAuditStore()
        val item = newStrictTrailerYardAudit("code-883", "title-15")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
