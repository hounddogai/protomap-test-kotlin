package com.acme.transport.crewing.dutyperiod.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientDutyPeriodProposalStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientDutyPeriodProposalStore()
        val item = newLenientDutyPeriodProposal("code-348", "name-321")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
