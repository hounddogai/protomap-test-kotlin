package com.acme.transport.emissions.carbonreport.reportrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalCarbonReportRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalCarbonReportRuleStore()
        val item = newRegionalCarbonReportRule("code-795", "label-262")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
