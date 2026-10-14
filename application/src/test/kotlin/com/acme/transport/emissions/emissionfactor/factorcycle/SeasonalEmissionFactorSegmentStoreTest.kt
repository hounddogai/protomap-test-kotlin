package com.acme.transport.emissions.emissionfactor.factorcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalEmissionFactorSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalEmissionFactorSegmentStore()
        val item = newSeasonalEmissionFactorSegment("code-475", "name-918")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
