package com.acme.transport.lastmile.drop.draft

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalDropAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalDropAuditStore()
        val item = newSeasonalDropAudit("code-307", "label-128")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
