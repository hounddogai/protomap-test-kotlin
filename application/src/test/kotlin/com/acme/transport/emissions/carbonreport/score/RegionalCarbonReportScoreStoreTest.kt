package com.acme.transport.emissions.carbonreport.score

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalCarbonReportScoreStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalCarbonReportScoreStore()
        val item = newRegionalCarbonReportScore("code-700", "title-115")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
