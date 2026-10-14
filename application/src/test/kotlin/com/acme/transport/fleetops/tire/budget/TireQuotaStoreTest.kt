package com.acme.transport.fleetops.tire.budget

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TireQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTireQuotaStore()
        val item = newTireQuota("code-315", "label-925")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
