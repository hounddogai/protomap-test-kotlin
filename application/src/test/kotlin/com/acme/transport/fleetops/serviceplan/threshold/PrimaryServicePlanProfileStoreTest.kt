package com.acme.transport.fleetops.serviceplan.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryServicePlanProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryServicePlanProfileStore()
        val item = newPrimaryServicePlanProfile("code-874", "label-361")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
