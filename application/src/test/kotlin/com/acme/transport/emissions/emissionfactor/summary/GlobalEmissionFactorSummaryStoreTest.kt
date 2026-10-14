package com.acme.transport.emissions.emissionfactor.summary

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalEmissionFactorSummaryStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalEmissionFactorSummaryStore()
        val item = newGlobalEmissionFactorSummary("code-493", "label-962")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
