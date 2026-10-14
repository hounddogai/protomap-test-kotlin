package com.acme.transport.lastmile.attempt.template

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class ManualAttemptQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryManualAttemptQuotaStore()
        val item = newManualAttemptQuota("code-574", "title-543")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
