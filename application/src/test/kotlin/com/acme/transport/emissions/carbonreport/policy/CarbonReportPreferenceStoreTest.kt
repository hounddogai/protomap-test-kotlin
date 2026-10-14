package com.acme.transport.emissions.carbonreport.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CarbonReportPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCarbonReportPreferenceStore()
        val item = newCarbonReportPreference("code-924", "label-735")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
