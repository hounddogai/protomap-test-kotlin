package com.acme.transport.freightpay.ratecard.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RollingRateCardPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRollingRateCardPolicyStore()
        val item = newRollingRateCardPolicy("code-467", "name-607")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
