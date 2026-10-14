package com.acme.transport.lastmile.drop.dropreservation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictDropAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictDropAuditStore()
        val item = newStrictDropAudit("code-712", "title-517")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
