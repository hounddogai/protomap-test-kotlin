package com.acme.transport.safety.coaching.baseline

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualCoachingBaselineStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualCoachingBaselineStore()
        val item = newManualCoachingBaseline("code-971", "name-519")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
