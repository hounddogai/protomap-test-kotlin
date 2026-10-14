package com.acme.transport.fleetops.serviceplan.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalServicePlanBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalServicePlanBaselineStore()
        val item = newRegionalServicePlanBaseline("code-497", "label-319")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
