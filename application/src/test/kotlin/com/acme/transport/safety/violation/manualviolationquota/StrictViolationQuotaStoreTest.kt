package com.acme.transport.safety.violation.manualviolationquota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class StrictViolationQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryStrictViolationQuotaStore()
        val item = newStrictViolationQuota("code-265", "title-774")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
