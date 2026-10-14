package com.acme.transport.safety.violation.allocation

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ViolationPreferenceStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryViolationPreferenceStore()
        val item = newViolationPreference("code-166", "title-661")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
