package com.acme.transport.fleetops.serviceplan.assignment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedServicePlanManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedServicePlanManifestStore()
        val item = newWeightedServicePlanManifest("code-935", "title-19")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
