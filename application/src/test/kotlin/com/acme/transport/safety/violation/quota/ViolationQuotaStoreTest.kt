package com.acme.transport.safety.violation.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ViolationQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryViolationQuotaStore()
        val item = newViolationQuota("code-291", "title-889")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
