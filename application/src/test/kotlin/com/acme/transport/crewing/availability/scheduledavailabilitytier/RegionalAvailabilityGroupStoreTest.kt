package com.acme.transport.crewing.availability.scheduledavailabilitytier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalAvailabilityGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalAvailabilityGroupStore()
        val item = newRegionalAvailabilityGroup("code-26", "title-880")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
