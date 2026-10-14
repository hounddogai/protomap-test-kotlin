package com.acme.transport.customs.tariff.notice

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveTariffNoticeStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveTariffNoticeStore()
        val item = newAdaptiveTariffNotice("code-519", "name-918")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
