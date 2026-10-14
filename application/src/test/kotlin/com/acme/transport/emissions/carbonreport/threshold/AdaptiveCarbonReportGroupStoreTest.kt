package com.acme.transport.emissions.carbonreport.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveCarbonReportGroupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveCarbonReportGroupStore()
        val item = newAdaptiveCarbonReportGroup("code-491", "title-30")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
