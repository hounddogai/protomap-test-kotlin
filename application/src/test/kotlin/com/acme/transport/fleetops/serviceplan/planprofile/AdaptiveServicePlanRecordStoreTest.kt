package com.acme.transport.fleetops.serviceplan.planprofile

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveServicePlanRecordStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveServicePlanRecordStore()
        val item = newAdaptiveServicePlanRecord("code-29", "name-327")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
