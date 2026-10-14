package com.acme.transport.linehaul.laneplan.rule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class CappedLanePlanPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryCappedLanePlanPolicyStore()
        val item = newCappedLanePlanPolicy("code-707", "name-921")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
