package com.acme.transport.emissions.carbonreport.claim

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CarbonReportClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCarbonReportClaimStore()
        val item = newCarbonReportClaim("code-958", "label-101")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
