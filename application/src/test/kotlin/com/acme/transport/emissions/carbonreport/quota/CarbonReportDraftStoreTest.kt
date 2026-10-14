package com.acme.transport.emissions.carbonreport.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CarbonReportDraftStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCarbonReportDraftStore()
        val item = newCarbonReportDraft("code-325", "name-702")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
