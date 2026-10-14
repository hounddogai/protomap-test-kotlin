package com.acme.transport.emissions.carbonreport.reportmapping

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryCarbonReportPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryCarbonReportPolicyStore()
        val item = newPrimaryCarbonReportPolicy("code-119", "name-867")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
