package com.acme.transport.linehaul.laneplan.planrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LanePlanProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLanePlanProposalStore()
        val item = newLanePlanProposal("code-717", "title-867")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
