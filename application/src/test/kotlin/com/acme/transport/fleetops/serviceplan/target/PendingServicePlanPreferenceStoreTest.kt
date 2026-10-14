package com.acme.transport.fleetops.serviceplan.target

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingServicePlanPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingServicePlanPreferenceStore()
        val item = newPendingServicePlanPreference("code-735", "title-873")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
