package com.acme.transport.linehaul.tender.tenderforecast

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AdaptiveTenderAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAdaptiveTenderAgreementStore()
        val item = newAdaptiveTenderAgreement("code-11", "title-843")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
