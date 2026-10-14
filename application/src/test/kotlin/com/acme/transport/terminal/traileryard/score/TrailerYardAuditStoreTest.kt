package com.acme.transport.terminal.traileryard.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TrailerYardAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTrailerYardAuditStore()
        val item = newTrailerYardAudit("code-194", "name-734")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
