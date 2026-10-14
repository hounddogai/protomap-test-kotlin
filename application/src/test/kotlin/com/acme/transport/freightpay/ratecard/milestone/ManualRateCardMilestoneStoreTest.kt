package com.acme.transport.freightpay.ratecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualRateCardMilestoneStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualRateCardMilestoneStore()
        val item = newManualRateCardMilestone("code-245", "label-753")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
