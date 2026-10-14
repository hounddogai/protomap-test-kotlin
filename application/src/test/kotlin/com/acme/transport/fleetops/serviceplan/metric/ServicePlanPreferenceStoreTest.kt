package com.acme.transport.fleetops.serviceplan.metric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ServicePlanPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryServicePlanPreferenceStore()
        val item = newServicePlanPreference("code-168", "name-404")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
