package com.acme.transport.freightpay.ratecard.request

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class PrimaryRateCardQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryPrimaryRateCardQuotaStore()
        val item = newPrimaryRateCardQuota("code-589", "title-122")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
