package com.acme.transport.fleetops.tire.profile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalTireRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalTireRecordStore()
        val item = newSeasonalTireRecord("code-988", "label-4")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
