package com.acme.transport.emissions.carbonreport.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CarbonReportManifestStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCarbonReportManifestStore()
        val item = newCarbonReportManifest("code-54", "title-595")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
