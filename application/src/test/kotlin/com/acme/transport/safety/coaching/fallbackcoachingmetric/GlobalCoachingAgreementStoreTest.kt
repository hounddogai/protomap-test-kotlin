package com.acme.transport.safety.coaching.fallbackcoachingmetric

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class GlobalCoachingAgreementStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryGlobalCoachingAgreementStore()
        val item = newGlobalCoachingAgreement("code-686", "title-273")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
