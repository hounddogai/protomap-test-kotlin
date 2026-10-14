package com.acme.transport.linehaul.laneplan.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LanePlanPolicyStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLanePlanPolicyStore()
        val item = newLanePlanPolicy("code-566", "name-921")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
