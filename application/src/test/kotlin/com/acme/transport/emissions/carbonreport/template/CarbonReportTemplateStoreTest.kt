package com.acme.transport.emissions.carbonreport.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CarbonReportTemplateStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCarbonReportTemplateStore()
        val item = newCarbonReportTemplate("code-931", "title-422")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
