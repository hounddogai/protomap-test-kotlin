package com.acme.transport.fleetops.serviceplan.planthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalServicePlanManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalServicePlanManifestStore()
        val item = newGlobalServicePlanManifest("code-289", "label-515")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
