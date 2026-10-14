package com.acme.transport.fleetops.tire.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalTireQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalTireQuotaStore()
        val item = newSeasonalTireQuota("code-608", "name-247")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
