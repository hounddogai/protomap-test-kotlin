package com.acme.transport.linehaul.laneplan.plansegment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LanePlanClaimStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLanePlanClaimStore()
        val item = newLanePlanClaim("code-355", "title-392")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
