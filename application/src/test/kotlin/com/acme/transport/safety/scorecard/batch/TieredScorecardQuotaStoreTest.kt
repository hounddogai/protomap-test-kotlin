package com.acme.transport.safety.scorecard.batch

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class TieredScorecardQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryTieredScorecardQuotaStore()
        val item = newTieredScorecardQuota("code-635", "name-379")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
