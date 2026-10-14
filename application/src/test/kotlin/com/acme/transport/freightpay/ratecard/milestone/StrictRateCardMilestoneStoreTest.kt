package com.acme.transport.freightpay.ratecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictRateCardMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictRateCardMilestoneStore()
        val item = newStrictRateCardMilestone("code-619", "label-413")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
