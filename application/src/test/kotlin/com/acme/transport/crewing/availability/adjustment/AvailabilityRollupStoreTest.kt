package com.acme.transport.crewing.availability.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AvailabilityRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAvailabilityRollupStore()
        val item = newAvailabilityRollup("code-182", "label-516")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
