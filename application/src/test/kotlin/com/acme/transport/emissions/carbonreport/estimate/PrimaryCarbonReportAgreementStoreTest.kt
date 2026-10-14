package com.acme.transport.emissions.carbonreport.estimate

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryCarbonReportAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryCarbonReportAgreementStore()
        val item = newPrimaryCarbonReportAgreement("code-404", "label-662")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
