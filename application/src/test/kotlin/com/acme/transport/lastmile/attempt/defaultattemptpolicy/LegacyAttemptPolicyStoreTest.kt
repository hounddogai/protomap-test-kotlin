package com.acme.transport.lastmile.attempt.defaultattemptpolicy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LegacyAttemptPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLegacyAttemptPolicyStore()
        val item = newLegacyAttemptPolicy("code-385", "label-460")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
