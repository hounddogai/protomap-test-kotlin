package com.acme.transport.emissions.carbonreport.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingCarbonReportAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingCarbonReportAssignmentStore()
        val item = newRollingCarbonReportAssignment("code-347", "name-665")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
