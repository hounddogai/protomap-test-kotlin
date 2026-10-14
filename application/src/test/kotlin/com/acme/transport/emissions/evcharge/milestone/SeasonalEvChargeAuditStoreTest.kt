package com.acme.transport.emissions.evcharge.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalEvChargeAuditStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalEvChargeAuditStore()
        val item = newSeasonalEvChargeAudit("code-752", "label-603")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
