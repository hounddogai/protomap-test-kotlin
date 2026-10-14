package com.acme.transport.safety.coaching.agreement

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class RegionalCoachingAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryRegionalCoachingAgreementStore()
        val item = newRegionalCoachingAgreement("code-742", "label-274")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
