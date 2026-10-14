package com.acme.transport.fleetops.serviceplan.legacyplanquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalServicePlanTargetStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalServicePlanTargetStore()
        val item = newGlobalServicePlanTarget("code-326", "label-669")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
