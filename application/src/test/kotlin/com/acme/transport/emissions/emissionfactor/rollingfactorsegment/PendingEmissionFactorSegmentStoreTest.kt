package com.acme.transport.emissions.emissionfactor.rollingfactorsegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PendingEmissionFactorSegmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPendingEmissionFactorSegmentStore()
        val item = newPendingEmissionFactorSegment("code-820", "title-727")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
