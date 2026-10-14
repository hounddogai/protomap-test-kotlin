package com.acme.transport.emissions.carbonreport.adaptivereportrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackCarbonReportGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackCarbonReportGroupStore()
        val item = newFallbackCarbonReportGroup("code-6", "title-784")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
