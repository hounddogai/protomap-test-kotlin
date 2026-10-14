package com.acme.transport.safety.scorecard.defaultscorecardledger

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class WeightedScorecardProfileStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryWeightedScorecardProfileStore()
        val item = newWeightedScorecardProfile("code-234", "label-427")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
