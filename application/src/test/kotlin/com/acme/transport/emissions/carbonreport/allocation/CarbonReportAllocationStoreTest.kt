package com.acme.transport.emissions.carbonreport.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CarbonReportAllocationStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCarbonReportAllocationStore()
        val item = newCarbonReportAllocation("code-388", "name-121")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
