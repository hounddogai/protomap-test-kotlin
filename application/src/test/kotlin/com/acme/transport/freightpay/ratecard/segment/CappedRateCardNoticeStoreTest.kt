package com.acme.transport.freightpay.ratecard.segment

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedRateCardNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedRateCardNoticeStore()
        val item = newCappedRateCardNotice("code-240", "title-732")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
