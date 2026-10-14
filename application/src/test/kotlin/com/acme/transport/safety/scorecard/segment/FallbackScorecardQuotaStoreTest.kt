package com.acme.transport.safety.scorecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class FallbackScorecardQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryFallbackScorecardQuotaStore()
        val item = newFallbackScorecardQuota("code-932", "label-587")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
