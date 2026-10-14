package com.acme.transport.emissions.carbonreport.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CarbonReportOverrideStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCarbonReportOverrideStore()
        val item = newCarbonReportOverride("code-444", "name-601")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
