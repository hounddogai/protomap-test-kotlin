package com.acme.transport.emissions.carbonreport.rollup

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingCarbonReportWindowStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingCarbonReportWindowStore()
        val item = newPendingCarbonReportWindow("code-525", "name-18")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
