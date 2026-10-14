package com.acme.transport.freightpay.ratecard.milestone

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ScheduledRateCardRollupStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryScheduledRateCardRollupStore()
        val item = newScheduledRateCardRollup("code-488", "name-751")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
