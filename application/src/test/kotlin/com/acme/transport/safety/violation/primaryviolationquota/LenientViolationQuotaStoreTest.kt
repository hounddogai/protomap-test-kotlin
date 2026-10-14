package com.acme.transport.safety.violation.primaryviolationquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientViolationQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientViolationQuotaStore()
        val item = newLenientViolationQuota("code-748", "title-20")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
