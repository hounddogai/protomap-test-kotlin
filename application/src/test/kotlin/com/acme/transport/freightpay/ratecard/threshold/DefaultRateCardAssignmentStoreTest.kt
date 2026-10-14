package com.acme.transport.freightpay.ratecard.threshold

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class DefaultRateCardAssignmentStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryDefaultRateCardAssignmentStore()
        val item = newDefaultRateCardAssignment("code-560", "name-310")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
