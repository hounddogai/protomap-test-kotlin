package com.acme.transport.safety.scorecard.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryScorecardThresholdStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryScorecardThresholdStore()
        val item = newPrimaryScorecardThreshold("code-39", "title-163")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
