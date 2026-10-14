package com.acme.transport.emissions.carbonreport.digest

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CarbonReportAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCarbonReportAssignmentStore()
        val item = newCarbonReportAssignment("code-220", "name-189")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
