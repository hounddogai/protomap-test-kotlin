package com.acme.transport.linehaul.laneplan.policy

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LanePlanRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLanePlanRuleStore()
        val item = newLanePlanRule("code-872", "name-599")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
