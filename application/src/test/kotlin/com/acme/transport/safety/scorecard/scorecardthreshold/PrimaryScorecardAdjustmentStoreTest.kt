package com.acme.transport.safety.scorecard.scorecardthreshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryScorecardAdjustmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryScorecardAdjustmentStore()
        val item = newPrimaryScorecardAdjustment("code-414", "label-336")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
