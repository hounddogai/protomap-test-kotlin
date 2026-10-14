package com.acme.transport.freightpay.ratecard.group

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyRateCardAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyRateCardAssignmentStore()
        val item = newLegacyRateCardAssignment("code-370", "name-634")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
