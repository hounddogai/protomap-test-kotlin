package com.acme.transport.freightpay.ratecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedRateCardPlanStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedRateCardPlanStore()
        val item = newCappedRateCardPlan("code-231", "name-81")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
