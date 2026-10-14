package com.acme.transport.emissions.carbonreport.reportestimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingCarbonReportOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingCarbonReportOverrideStore()
        val item = newRollingCarbonReportOverride("code-765", "label-183")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
