package com.acme.transport.emissions.emissionfactor.factorcycle

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class SeasonalEmissionFactorCheckpointStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemorySeasonalEmissionFactorCheckpointStore()
        val item = newSeasonalEmissionFactorCheckpoint("code-316", "label-561")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
