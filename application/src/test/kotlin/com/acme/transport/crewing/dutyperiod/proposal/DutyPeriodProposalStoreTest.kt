package com.acme.transport.crewing.dutyperiod.proposal

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DutyPeriodProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDutyPeriodProposalStore()
        val item = newDutyPeriodProposal("code-566", "title-46")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
