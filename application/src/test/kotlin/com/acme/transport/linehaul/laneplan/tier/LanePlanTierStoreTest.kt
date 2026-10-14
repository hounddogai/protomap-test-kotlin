package com.acme.transport.linehaul.laneplan.tier

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LanePlanTierStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLanePlanTierStore()
        val item = newLanePlanTier("code-695", "name-714")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
