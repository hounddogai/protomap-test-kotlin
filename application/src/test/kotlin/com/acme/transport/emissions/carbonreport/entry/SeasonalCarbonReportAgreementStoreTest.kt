package com.acme.transport.emissions.carbonreport.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalCarbonReportAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalCarbonReportAgreementStore()
        val item = newSeasonalCarbonReportAgreement("code-325", "name-370")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
