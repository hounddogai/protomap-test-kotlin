package com.acme.transport.lastmile.parcel.review

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedParcelThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedParcelThresholdStore()
        val item = newWeightedParcelThreshold("code-380", "title-750")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
