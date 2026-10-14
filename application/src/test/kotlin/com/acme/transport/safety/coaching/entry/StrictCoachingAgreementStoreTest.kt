package com.acme.transport.safety.coaching.entry

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictCoachingAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictCoachingAgreementStore()
        val item = newStrictCoachingAgreement("code-928", "label-270")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
