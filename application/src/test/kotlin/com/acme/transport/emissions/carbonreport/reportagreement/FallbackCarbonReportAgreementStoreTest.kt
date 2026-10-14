package com.acme.transport.emissions.carbonreport.reportagreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackCarbonReportAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackCarbonReportAgreementStore()
        val item = newFallbackCarbonReportAgreement("code-808", "title-764")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
