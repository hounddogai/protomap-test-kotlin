package com.acme.transport.linehaul.laneplan.planrule

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

class LenientLanePlanRuleStoreTest {
    @Test
    fun `finds what it saved`() {
        val store = newMemoryLenientLanePlanRuleStore()
        val item = newLenientLanePlanRule("code-396", "title-352")
        store.save(item)
        val found = store.find(item.code) ?: fail("missing result")
        assertEquals(item.code, found.code, "code")
        assertEquals(1, store.list().size, "count")
    }
}
