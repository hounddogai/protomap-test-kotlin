package com.acme.transport.lastmile.attempt.quota

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class AttemptQuotaStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryAttemptQuotaStore()
        val item = newAttemptQuota("code-144", "title-932")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
