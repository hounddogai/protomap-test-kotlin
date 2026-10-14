package com.acme.transport.crewing.availability.availabilitysegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingAvailabilityGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingAvailabilityGroupStore()
        val item = newRollingAvailabilityGroup("code-810", "title-593")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
